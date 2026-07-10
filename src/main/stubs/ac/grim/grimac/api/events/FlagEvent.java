package ac.grim.grimac.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Stub for building without Grim JAR. */
public class FlagEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() { return handlers; }
    public HandlerList getHandlers() { return handlers; }

    public Player getPlayer() { return null; }
    public Check getCheck() { return new Check(); }

    public static class Check {
        public String getCheckName() { return ""; }
        public double getViolations() { return 0; }
        public double getDecay() { return 0; }
    }
}
