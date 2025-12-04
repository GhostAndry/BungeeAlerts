package me.ghostdevelopment.bungeealerts.events;

import me.frep.vulcan.api.event.VulcanFlagEvent;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.Check;
import me.ghostdevelopment.bungeealerts.utils.DB;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Timestamp;
import java.util.Locale;

public class onVulcanFlagEvent implements Listener {
    private final BungeeAlerts plugin;

    public onVulcanFlagEvent(BungeeAlerts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFlagEvent(VulcanFlagEvent event) {
        String serverName = this.plugin.getConfig().getString("server_name");
        String playerName = event.getPlayer().getName();
        String checkValue = (event.getCheck().getName()+ " ( Type: " + event.getCheck().getType() + ")").toUpperCase();
        int violations = event.getCheck().getVl();

        // Generate timestamp for consistent timing
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String timeString = now.toString();
        if (timeString.length() > 23) {
            timeString = timeString.substring(0, 23);
        }

        String description = "Vulcan " + checkValue + " Detection";
        String checkInfo = "Violation Level: " + violations +
                "\nDetection Type: " + (event.getCheck().getName()+ " ( Type: " + event.getCheck().getType() + ")").toUpperCase() +
                "\nPlayer: " + playerName +
                "\nServer: " + serverName +
                "\nTimestamp: " + timeString +
                "\nBuffer: " + event.getCheck().getBuffer()+
                "\nBuffer Decay: " + event.getCheck().getBufferDecay()+
                "\nDescription: " + event.getCheck().getDescription()
                ;

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