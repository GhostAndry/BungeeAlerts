package me.frep.vulcan.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Stub for building without Vulcan JAR. */
public class VulcanFlagEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() { return handlers; }
    public HandlerList getHandlers() { return handlers; }

    public Player getPlayer() { return null; }
    public Check getCheck() { return new Check(); }

    public static class Check {
        public String getName() { return ""; }
        public String getType() { return ""; }
        public int getVl() { return 0; }
        public double getBuffer() { return 0; }
        public double getBufferDecay() { return 0; }
        public String getDescription() { return ""; }
    }
}
