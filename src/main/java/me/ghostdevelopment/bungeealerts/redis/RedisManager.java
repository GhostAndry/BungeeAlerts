package me.ghostdevelopment.bungeealerts.redis;

import com.google.gson.Gson;
import me.ghostdevelopment.bungeealerts.AlertDispatcher;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.Settings;
import me.ghostdevelopment.bungeealerts.utils.Check;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Redis pub/sub manager for cross-server alert synchronization.
 * <p>
 * Publishes {@link Check} objects as JSON to a shared Redis channel
 * and listens for incoming alerts from other servers in the network.
 * <p>
 * The listener runs on a daemon thread with automatic reconnection
 * on connection loss (5-second retry delay).
 */
public final class RedisManager {

    private final String host;
    private final int port;
    private final String password;
    private final String channel;
    private final Gson gson = new Gson();
    private final BungeeAlerts plugin;
    private final AlertDispatcher dispatcher;
    private final Logger logger;
    private volatile boolean running = true;

    public RedisManager(BungeeAlerts plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.dispatcher = BungeeAlerts.getDispatcher();
        this.host = plugin.getConfig().getString(
                Settings.CFG_REDIS_HOST, Settings.DEFAULT_REDIS_HOST);
        this.port = plugin.getConfig().getInt(
                Settings.CFG_REDIS_PORT, Settings.DEFAULT_REDIS_PORT);
        this.password = plugin.getConfig().getString(Settings.CFG_REDIS_PASSWORD, null);
        this.channel = plugin.getConfig().getString(
                Settings.CFG_REDIS_CHANNEL, Settings.DEFAULT_REDIS_CHANNEL);

        logger.info("RedisManager initialized. Host: " + host
                + ", Port: " + port + ", Channel: " + channel
                + (password != null ? ", Auth: enabled" : ", Auth: none"));
    }

    /** Creates a new Jedis connection with authentication if configured. */
    private Jedis createJedis() {
        Jedis jedis = new Jedis(host, port);
        if (password != null && !password.isEmpty()) {
            jedis.auth(password);
        }
        return jedis;
    }

    /**
     * Serializes a {@link Check} to JSON and publishes it on the Redis channel.
     * Also delivers the alert to local staff via {@link AlertDispatcher#sendToStaff}.
     */
    public void sendCheck(Check check) {
        try (Jedis jedis = createJedis()) {
            jedis.publish(channel, gson.toJson(check));
            dispatcher.sendToStaff(check);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending Check to Redis: " + e.getMessage(), e);
        }
    }

    /**
     * Publishes a plain-text message on the Redis channel and to local staff.
     * Used for status messages (e.g. "BungeeAlerts enabled on server X").
     */
    public void sendMessage(String message) {
        try (Jedis jedis = createJedis()) {
            jedis.publish(channel, message);
            dispatcher.sendPlainToStaff(message);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error sending message to Redis: " + e.getMessage(), e);
        }
    }

    /**
     * Starts the Redis subscription listener on a daemon thread.
     * Incoming messages are deserialized as {@link Check} JSON;
     * plain-text fallback is used when parsing fails.
     */
    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                try (Jedis jedis = createJedis()) {
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String remoteChannel, String message) {
                            if (!remoteChannel.trim().equalsIgnoreCase(channel.trim())) {
                                return;
                            }
                            Bukkit.getScheduler().runTask(plugin, () -> handleIncoming(message));
                        }

                        @Override
                        public void onSubscribe(String ch, int subscribedChannels) {
                            logger.info("Subscribed to Redis channel: " + ch);
                        }
                    }, channel);
                } catch (Exception e) {
                    if (!running) break;
                    logger.severe("Redis connection error: " + e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "Redis-Listener-Thread");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /** Signals the listener thread to stop. Called during plugin shutdown. */
    public void shutdown() {
        running = false;
    }

    // ── internal ─────────────────────────────────────────────

    private void handleIncoming(String message) {
        try {
            Check check = gson.fromJson(message, Check.class);
            if (check != null && check.getPlayerName() != null) {
                dispatcher.sendToStaff(check);
            } else {
                dispatcher.sendPlainToStaff(message);
            }
        } catch (Exception ex) {
            logger.warning("JSON parsing failed. Fallback to plain message: " + ex.getMessage());
            dispatcher.sendPlainToStaff(message);
        }
    }
}
