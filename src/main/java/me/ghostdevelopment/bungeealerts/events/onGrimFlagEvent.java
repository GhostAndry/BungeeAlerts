package me.ghostdevelopment.bungeealerts.events;

import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import ac.grim.grimac.api.events.FlagEvent;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.DB;
import net.md_5.bungee.api.ChatColor;

public class onGrimFlagEvent implements Listener {
    private final BungeeAlerts plugin;

    public onGrimFlagEvent(BungeeAlerts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFlagEvent(FlagEvent event) {
        String serverName = this.plugin.getConfig().getString("server_name");
        String flag = this.plugin.getConfig().getString("alert_message")
                .replaceAll("%player%", event.getPlayer().getName())
                .replaceAll("%check%", event.getCheck().getCheckName().toUpperCase(Locale.ROOT))
                .replaceAll("%vl%", String.valueOf(event.getViolations()))
                .replaceAll("%server%", serverName);
        flag = ChatColor.translateAlternateColorCodes('&', flag);

        Bukkit.getLogger().info(removeCharAndNext(flag));

        BungeeAlerts.getRedisManager().sendMessage(flag);

        for (Player staffer : BungeeAlerts.getStaffer()) {
            staffer.sendMessage(flag);
        }

        DB.add(event.getPlayer().getName(), event.getCheck().getCheckName().toUpperCase(), (int) event.getViolations(), serverName);
    }

    public static String removeCharAndNext(String str) {
        return str.replaceAll("&.", "");
    }
}
