package me.ghostdevelopment.bungeealerts.events;

import me.frep.vulcan.api.event.VulcanFlagEvent;
import me.ghostdevelopment.bungeealerts.AlertDispatcher;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.InternalFlag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for Vulcan anti-cheat flag events and dispatches alerts.
 *
 * @see VulcanFlagEvent
 */
public final class onVulcanFlagEvent implements Listener {

    private final AlertDispatcher dispatcher;

    public onVulcanFlagEvent(BungeeAlerts plugin) {
        this.dispatcher = BungeeAlerts.getDispatcher();
    }

    @EventHandler
    public void onFlagEvent(VulcanFlagEvent event) {
        String playerName = event.getPlayer().getName();
        String checkValue = (event.getCheck().getName()
                + " ( Type: " + event.getCheck().getType() + ")").toUpperCase();
        int violations = event.getCheck().getVl();

        InternalFlag flag = new InternalFlag(
                playerName,
                checkValue,
                violations,
                "Vulcan " + checkValue + " Detection",
                "Violation Level: " + violations
                        + "\nDetection Type: " + checkValue
                        + "\nPlayer: " + playerName
                        + "\nBuffer: " + event.getCheck().getBuffer()
                        + "\nBuffer Decay: " + event.getCheck().getBufferDecay()
                        + "\nDescription: " + event.getCheck().getDescription()
        );

        dispatcher.dispatch(flag);
    }
}
