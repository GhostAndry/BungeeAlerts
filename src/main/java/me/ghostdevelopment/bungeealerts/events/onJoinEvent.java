package me.ghostdevelopment.bungeealerts.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;

public class onJoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(BungeeAlerts.getInstance(), ()-> setAlerts(event), 10);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(BungeeAlerts.getInstance(), ()-> setAlerts(event), 10);
    }
    
    private void setAlerts(PlayerQuitEvent event) {
        if(event.getPlayer().isOp()||event.getPlayer().hasPermission("bungeealerts.use")) {
            BungeeAlerts.getStaffer().remove(event.getPlayer());
        }
    }
    private void setAlerts(PlayerJoinEvent event) {
        if(event.getPlayer().isOp()||event.getPlayer().hasPermission("bungeealerts.use")) {
            BungeeAlerts.getStaffer().add(event.getPlayer());
        }
    }
    
}
