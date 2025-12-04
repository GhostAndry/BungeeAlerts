package me.ghostdevelopment.bungeealerts.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;

public class onJoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("bungeealerts.use") || player.isOp()) {
            BungeeAlerts.getStafferMap().putIfAbsent(player.getUniqueId(), true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BungeeAlerts.getStafferMap().remove(player.getUniqueId());
    }


}
