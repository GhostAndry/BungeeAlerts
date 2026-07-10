package me.ghostdevelopment.bungeealerts.events;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Tracks staff join/quit to maintain the alert recipients set.
 * <p>
 * Players with {@code bungeealerts.use} permission or OP status
 * are automatically added to the staff set on join and removed on quit.
 */
public final class onJoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("bungeealerts.use") || player.isOp()) {
            BungeeAlerts.getStaffSet().add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BungeeAlerts.getStaffSet().remove(event.getPlayer().getUniqueId());
    }
}
