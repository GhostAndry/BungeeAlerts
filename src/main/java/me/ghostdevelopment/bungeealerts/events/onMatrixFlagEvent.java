package me.ghostdevelopment.bungeealerts.events;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.redis.RedisManager;
import me.ghostdevelopment.bungeealerts.utils.Check;
import me.ghostdevelopment.bungeealerts.utils.DB;
import me.rerere.matrix.api.events.PlayerViolationEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Timestamp;
import java.util.Locale;

public class onMatrixFlagEvent implements Listener {
    private final BungeeAlerts plugin;

    public onMatrixFlagEvent(BungeeAlerts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFlagEvent(PlayerViolationEvent event) {
        String serverName = this.plugin.getConfig().getString("server_name");
        String playerName = event.getPlayer().getName();
        String checkValue = event.getHackType().name().toUpperCase(Locale.ROOT);
        int violations = event.getViolations();

        // Generate timestamp for consistent timing
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String timeString = now.toString();
        if (timeString.length() > 23) {
            timeString = timeString.substring(0, 23);
        }

        // Create detailed description and info
        String description = "Matrix " + checkValue + " Detection";
        String checkInfo = "Violation Level: " + violations +
                "\nDetection Type: " + event.getHackType().name() +
                "\nPlayer: " + playerName +
                "\nServer: " + serverName +
                "\nTimestamp: " + timeString;

        // Create Check object with all details
        Check check = new Check(
                timeString,
                playerName,
                checkValue,
                violations,
                serverName,
                description,
                checkInfo
        );

        // Log to console (plain text version)
        Bukkit.getLogger().info(removeCharAndNext(
                plugin.getConfig().getString("alert_message")
                        .replace("%player%", playerName)
                        .replace("%check%", checkValue)
                        .replace("%vl%", String.valueOf(violations))
                        .replace("%server%", serverName)
        ));

        // Send to Redis (will handle hover message creation)
        BungeeAlerts.getRedisManager().sendCheck(check);

        // Add to database
        DB.add(check);
    }

    public static String removeCharAndNext(String str) {
        return str.replaceAll("&.", "");
    }
}