package me.ghostdevelopment.bungeealerts.events;

import me.ghostdevelopment.bungeealerts.AlertDispatcher;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.InternalFlag;
import me.rerere.matrix.api.events.PlayerViolationEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Locale;

/**
 * Listens for Matrix anti-cheat violation events and dispatches alerts.
 *
 * @see PlayerViolationEvent
 */
public final class onMatrixFlagEvent implements Listener {

    private final AlertDispatcher dispatcher;

    public onMatrixFlagEvent(BungeeAlerts plugin) {
        this.dispatcher = BungeeAlerts.getDispatcher();
    }

    @EventHandler
    public void onFlagEvent(PlayerViolationEvent event) {
        String playerName = event.getPlayer().getName();
        String checkValue = event.getHackType().name().toUpperCase(Locale.ROOT);
        int violations = event.getViolations();

        InternalFlag flag = new InternalFlag(
                playerName,
                checkValue,
                violations,
                "Matrix " + checkValue + " Detection",
                "Violation Level: " + violations
                        + "\nDetection Type: " + event.getHackType().name()
                        + "\nPlayer: " + playerName
        );

        dispatcher.dispatch(flag);
    }
}
