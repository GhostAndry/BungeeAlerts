package me.ghostdevelopment.bungeealerts.events;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.redis.RedisManager;
import me.ghostdevelopment.bungeealerts.utils.DB;
import me.rerere.matrix.api.events.PlayerViolationEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Locale;

public class onMatrixFlagEvent implements Listener {
    private final BungeeAlerts plugin;

    public onMatrixFlagEvent(BungeeAlerts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFlagEvent(PlayerViolationEvent event) {
        String serverName = this.plugin.getConfig().getString("server_name");
        String flag = this.plugin.getConfig().getString("alert_message")
                .replaceAll("%player%", event.getPlayer().getName())
                .replaceAll("%check%", event.getHackType().name().toUpperCase(Locale.ROOT))
                .replaceAll("%vl%", String.valueOf(event.getViolations()))
                .replaceAll("%server%", serverName);
        flag = ChatColor.translateAlternateColorCodes('&', flag);

        Bukkit.getLogger().info(removeCharAndNext(flag));

        BungeeAlerts.getRedisManager().sendMessage(flag);

        for (Player staffer : BungeeAlerts.getStaffer()) {
            staffer.sendMessage(flag);
        }

        DB.add(event.getPlayer().getName(), event.getHackType().name().toUpperCase(), event.getViolations(), serverName);
    }

    public static String removeCharAndNext(String str) {
        return str.replaceAll("&.", "");
    }
}
