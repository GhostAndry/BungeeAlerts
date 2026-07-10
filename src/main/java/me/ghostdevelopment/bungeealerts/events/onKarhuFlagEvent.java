package me.ghostdevelopment.bungeealerts.events;

import me.ghostdevelopment.bungeealerts.AlertDispatcher;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.InternalFlag;
import me.liwk.karhu.api.KarhuAPI;
import me.liwk.karhu.api.event.KarhuEvent;
import me.liwk.karhu.api.event.KarhuListener;
import me.liwk.karhu.api.event.impl.KarhuAlertEvent;

import java.util.Locale;

/**
 * Listens for Karhu anti-cheat alert events and dispatches alerts.
 * <p>
 * Unlike other handlers, Karhu uses its own {@link KarhuListener} API
 * instead of Bukkit events. Registered via {@link KarhuAPI#getEventRegistry()}.
 *
 * @see KarhuAlertEvent
 */
public final class onKarhuFlagEvent implements KarhuListener {

    private final AlertDispatcher dispatcher;

    public onKarhuFlagEvent(BungeeAlerts plugin) {
        this.dispatcher = BungeeAlerts.getDispatcher();
    }

    private void onFlagEvent(KarhuAlertEvent event) {
        String playerName = event.getPlayer().getName();
        String checkValue = event.getCheck().getName().toUpperCase(Locale.ROOT)
                + " (" + event.getCheck().getSubCategory() + ")";
        int violations = event.getViolations();

        InternalFlag flag = new InternalFlag(
                playerName,
                checkValue,
                violations,
                "Karhu " + checkValue + " Detection",
                "Violation Level: " + violations
                        + "\nDetection Type: " + checkValue
                        + "\nPlayer: " + playerName
                        + "\nDescription: " + event.getCheck().getDesc()
                        + "\nDebug: " + event.getDebug()
        );

        dispatcher.dispatch(flag);
    }

    @Override
    public void onEvent(KarhuEvent karhuEvent) {
        if (karhuEvent instanceof KarhuAlertEvent alertEvent) {
            onFlagEvent(alertEvent);
        }
    }

    /**
     * Registers this listener with the Karhu event registry.
     * Must be called after verifying Karhu is present on the server.
     */
    public static void register(BungeeAlerts plugin) {
        KarhuAPI.getEventRegistry().addListener(new onKarhuFlagEvent(plugin));
    }
}
