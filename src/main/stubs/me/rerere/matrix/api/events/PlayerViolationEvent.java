package me.rerere.matrix.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Stub for building without Matrix JAR. */
public class PlayerViolationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    public static HandlerList getHandlerList() { return handlers; }
    public HandlerList getHandlers() { return handlers; }

    public Player getPlayer() { return null; }
    public HackType getHackType() { return HackType.UNKNOWN; }
    public int getViolations() { return 0; }

    public enum HackType { UNKNOWN }
}
