package me.ghostdevelopment.bungeealerts.events;

import me.frep.vulcan.api.event.VulcanFlagEvent;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.redis.RedisManager;
import me.ghostdevelopment.bungeealerts.utils.DB;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Locale;

public class onVulcanFlagEvent implements Listener {
    private final BungeeAlerts plugin;

    public onVulcanFlagEvent(BungeeAlerts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFlagEvent(VulcanFlagEvent event) {
        String serverName = this.plugin.getConfig().getString("server_name");
        String check = event.getCheck().getName().toUpperCase(Locale.ROOT)+" "+event.getCheck().getType();
        String flag = this.plugin.getConfig().getString("alert_message")
                .replaceAll("%player%", event.getPlayer().getName())
                .replaceAll("%check%", check.toUpperCase(Locale.ROOT))
                .replaceAll("%vl%", String.valueOf(event.getCheck().getVl()))
                .replaceAll("%server%", serverName);
        flag = ChatColor.translateAlternateColorCodes('&', flag);

        Bukkit.getLogger().info(removeCharAndNext(flag));

        BungeeAlerts.getRedisManager().sendMessage(flag);

        for (Player staffer : BungeeAlerts.getStaffer()) {
            staffer.sendMessage(flag);
        }

        DB.add(event.getPlayer().getName(), event.getCheck().getName().toUpperCase(), event.getCheck().getVl(), serverName);
    }

    public static String removeCharAndNext(String str) {
        return str.replaceAll("&.", "");
    }
}
