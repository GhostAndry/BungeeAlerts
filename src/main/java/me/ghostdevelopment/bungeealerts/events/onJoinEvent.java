package me.ghostdevelopment.bungeealerts.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;

public class onJoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        
        if(event.getPlayer().isOp()||event.getPlayer().hasPermission("bungeealerts.use")) {
            BungeeAlerts.getStaffer().add(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        
        if(event.getPlayer().isOp()||event.getPlayer().hasPermission("bungeealerts.use")) {
            BungeeAlerts.getStaffer().remove(event.getPlayer());
        }
    }
    
}
