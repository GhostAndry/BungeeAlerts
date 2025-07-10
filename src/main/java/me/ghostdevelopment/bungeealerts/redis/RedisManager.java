package me.ghostdevelopment.bungeealerts.redis;

import com.google.gson.Gson;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.Check;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.logging.Level;

public class RedisManager {

    private final String host;
    private final int port;
    private final String channel;
    private final Gson gson = new Gson();
    private final BungeeAlerts plugin;

    public RedisManager(BungeeAlerts plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfig().getString("redis.host", "localhost");
        this.port = plugin.getConfig().getInt("redis.port", 6379);
        this.channel = plugin.getConfig().getString("redis.channel", "bungeealerts");

        plugin.getLogger().info("✅ RedisManager initialized. Host: " + host + ", Port: " + port + ", Channel: " + channel);
    }

    public void sendCheck(Check check) {
        try (Jedis jedis = new Jedis(host, port)) {
            String json = gson.toJson(check);
            jedis.publish(channel, json);

            sendCheckToStaff(check);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Error sending Check to Redis: " + e.getMessage(), e);
        }
    }

    public void sendMessage(String message) {
        try (Jedis jedis = new Jedis(host, port)) {
            jedis.publish(channel, message);

            sendPlainMessageToStaff(message);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "❌ Error sending message to Redis: " + e.getMessage(), e);
        }
    }

    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Jedis jedis = new Jedis(host, port)) {
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String remoteChannel, String message) {

                            if (!remoteChannel.trim().equalsIgnoreCase(channel.trim())) {
                                return;
                            }

                            Bukkit.getScheduler().runTask(plugin, () -> {
                                try {
                                    Check check = gson.fromJson(message, Check.class);
                                    if (check != null && check.getPlayerName() != null) {
                                        sendCheckToStaff(check);
                                    } else {
                                        sendPlainMessageToStaff(message);
                                    }
                                } catch (Exception ex) {
                                    plugin.getLogger().warning("⚠️ JSON parsing failed. Fallback to plain message: " + ex.getMessage());
                                    sendPlainMessageToStaff(message);
                                }
                            });
                        }

                        @Override
                        public void onSubscribe(String ch, int subscribedChannels) {
                            plugin.getLogger().info("✅ Subscribed to Redis channel: " + ch);
                        }
                    }, channel);
                } catch (Exception e) {
                    plugin.getLogger().severe("❌ Redis connection error: " + e.getMessage());
                    try {
                        Thread.sleep(5000); // Retry delay
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }, "Redis-Listener-Thread");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void sendCheckToStaff(Check check) {
        String alertFormat = plugin.getConfig().getString("alert_message",
                "&8[&5&lAntiCheat&8] &7(%server%) &d%player% &7> &d%check% &8(&d%vl%&8)");
        String hoverFormat = plugin.getConfig().getString("aclogs.hover_message",
                "&6Player: &e%player%\n&6Check: &e%check%\n&6VL: &e%vl%\n&6Server: &e%server%");

        String formattedMessage = alertFormat
                .replace("%player%", check.getPlayerName())
                .replace("%check%", check.getCheck())
                .replace("%vl%", String.valueOf(check.getVl()))
                .replace("%server%", check.getServer());

        String hoverText = hoverFormat
                .replace("%player%", check.getPlayerName())
                .replace("%check%", check.getCheck())
                .replace("%vl%", String.valueOf(check.getVl()))
                .replace("%server%", check.getServer())
                .replace("%time%", check.getTime() != null ? check.getTime() : "N/A")
                .replace("%description%", check.getDescription() != null ? check.getDescription() : "N/A")
                .replace("%check_info%", check.getInfo() != null ? check.getInfo() : "N/A");

        TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', formattedMessage));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()));

        if (BungeeAlerts.getStaffer() != null && !BungeeAlerts.getStaffer().isEmpty()) {
            for (Player staffer : BungeeAlerts.getStaffer()) {
                if (staffer != null && staffer.isOnline()) {
                    staffer.spigot().sendMessage(component);
                }
            }
        }
    }

    private void sendPlainMessageToStaff(String message) {
        String formatted = ChatColor.translateAlternateColorCodes('&', message);

        if (BungeeAlerts.getStaffer() != null && !BungeeAlerts.getStaffer().isEmpty()) {
            for (Player staffer : BungeeAlerts.getStaffer()) {
                if (staffer != null && staffer.isOnline()) {
                    staffer.sendMessage(formatted);
                }
            }
        }
    }
}
