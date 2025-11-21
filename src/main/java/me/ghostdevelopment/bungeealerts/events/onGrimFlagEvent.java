package me.ghostdevelopment.bungeealerts.events;

import ac.grim.grimac.api.events.FlagEvent;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.redis.RedisManager;
import me.ghostdevelopment.bungeealerts.utils.Check;
import me.ghostdevelopment.bungeealerts.utils.DB;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Timestamp;
import java.util.Locale;

public class onGrimFlagEvent implements Listener {
    private final BungeeAlerts plugin;

    public onGrimFlagEvent(BungeeAlerts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFlagEvent(FlagEvent event) {
        String serverName = this.plugin.getConfig().getString("server_name");
        String playerName = event.getPlayer().getName();
        String checkValue = event.getCheck().getCheckName().toUpperCase(Locale.ROOT);
        int violations = Integer.parseInt(String.valueOf(event.getCheck().getViolations()));

        Timestamp now = new Timestamp(System.currentTimeMillis());
        String timeString = now.toString();
        if (timeString.length() > 23) {
            timeString = timeString.substring(0, 23);
        }

        String description = "Matrix " + checkValue + " Detection";
        String checkInfo = "Violation Level: " + violations +
                "\nDetection Type: " + event.getCheck().getCheckName() +
                "\nPlayer: " + playerName +
                "\nServer: " + serverName +
                "\nTimestamp: " + timeString +
                "\nDecay: " + event.getCheck().getDecay()
                ;

        Check check = new Check(
                timeString,
                playerName,
                checkValue,
                violations,
                serverName,
                description,
                checkInfo
        );

        Bukkit.getLogger().info(removeCharAndNext(
                plugin.getConfig().getString("alert_message")
                        .replace("%player%", playerName)
                        .replace("%check%", checkValue)
                        .replace("%vl%", String.valueOf(violations))
                        .replace("%server%", serverName)
        ));

        BungeeAlerts.getRedisManager().sendCheck(check);

        DB.add(check);
    }

    public static String removeCharAndNext(String str) {
        return str.replaceAll("&.", "");
    }
}