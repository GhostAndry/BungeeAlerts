package me.ghostdevelopment.bungeealerts.events;

import ac.grim.grimac.api.events.FlagEvent;
import me.ghostdevelopment.bungeealerts.AlertDispatcher;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.InternalFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Locale;

/**
 * Listens for GrimAC flag events and dispatches alerts.
 *
 * @see FlagEvent
 */
public final class onGrimFlagEvent implements Listener {

    private final AlertDispatcher dispatcher;

    public onGrimFlagEvent(BungeeAlerts plugin) {
        this.dispatcher = BungeeAlerts.getDispatcher();
    }

    @EventHandler
    public void onFlagEvent(FlagEvent event) {
        String playerName = event.getPlayer().getName();
        String checkValue = event.getCheck().getCheckName().toUpperCase(Locale.ROOT);
        int violations = (int) event.getCheck().getViolations();

        InternalFlag flag = new InternalFlag(
                playerName,
                checkValue,
                violations,
                "Grim " + checkValue + " Detection",
                "Violation Level: " + violations
                        + "\nDetection Type: " + event.getCheck().getCheckName()
                        + "\nPlayer: " + playerName
                        + "\nDecay: " + event.getCheck().getDecay()
        );

        dispatcher.dispatch(flag);
    }
}
