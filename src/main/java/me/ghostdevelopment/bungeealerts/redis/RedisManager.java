package me.ghostdevelopment.bungeealerts.redis;

import java.util.logging.Level;

import lombok.Getter;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisManager {

    private final String host;
    private final int port;
    @Getter private final String channel;

    public RedisManager(BungeeAlerts plugin) {
        this.host = plugin.getConfig().getString("redis.host", "localhost");
        this.port = plugin.getConfig().getInt("redis.port", 6379);
        this.channel = plugin.getConfig().getString("redis.channel");
    }

    public void sendMessage(String message) {
        try (Jedis jedis = new Jedis(host, port)) {
            jedis.publish(channel, message);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "[BungeeAlerts Redis] Error to send the message: {0}", e.getMessage());
        }
    }

    public void startListening() {
        new Thread(() -> {
            try (Jedis jedis = new Jedis(host, port)) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String remotechannel, String message) {
                        if (remotechannel.equals(channel)) {
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }
                    }
                }, channel);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "[BungeeAlerts Redis] Error to receive the message: {0}", e.getMessage());
            }
        }).start();
    }
}
