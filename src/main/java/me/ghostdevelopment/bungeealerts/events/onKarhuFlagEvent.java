package me.ghostdevelopment.bungeealerts.events;

import me.frep.vulcan.api.event.VulcanFlagEvent;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.Check;
import me.ghostdevelopment.bungeealerts.utils.DB;
import me.liwk.karhu.api.KarhuAPI;
import me.liwk.karhu.api.event.KarhuEvent;
import me.liwk.karhu.api.event.KarhuListener;
import me.liwk.karhu.api.event.impl.KarhuAlertEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import java.sql.Timestamp;
import java.util.Locale;

public class onKarhuFlagEvent implements KarhuListener {

    private final BungeeAlerts plugin;

    public onKarhuFlagEvent(BungeeAlerts plugin) {
        this.plugin = plugin;
    }

    public void onFlagEvent(KarhuAlertEvent event) {
        String serverName = this.plugin.getConfig().getString("server_name");
        String playerName = event.getPlayer().getName();
        String checkValue = event.getCheck().getName().toUpperCase(Locale.ROOT)+ " (" + event.getCheck().getSubCategory() + ")";
        int violations = event.getViolations();

        // Generate timestamp for consistent timing
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String timeString = now.toString();
        if (timeString.length() > 23) {
            timeString = timeString.substring(0, 23);
        }

        String description = "Karhu " + checkValue + " Detection";
        String checkInfo = "Violation Level: " + violations +
                "\nDetection Type: " + event.getCheck().getName().toUpperCase(Locale.ROOT)+ " (" + event.getCheck().getSubCategory() + ")" +
                "\nPlayer: " + playerName +
                "\nServer: " + serverName +
                "\nTimestamp: " + timeString +
                "\nDescription: " + event.getCheck().getDesc()+
                "\nDebug: " + event.getDebug()
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

    @Override
    public void onEvent(KarhuEvent karhuEvent) {
        if( karhuEvent instanceof KarhuAlertEvent ) {
            onFlagEvent((KarhuAlertEvent) karhuEvent);
        }
    }

    public static void register(BungeeAlerts plugin) {
        KarhuAPI.getEventRegistry().addListener(new onKarhuFlagEvent(plugin));
    }
}
