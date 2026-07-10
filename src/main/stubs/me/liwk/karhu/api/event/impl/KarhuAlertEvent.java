package me.liwk.karhu.api.event.impl;

import me.liwk.karhu.api.event.KarhuEvent;
import org.bukkit.entity.Player;

/** Stub for building without Karhu JAR. */
public class KarhuAlertEvent extends KarhuEvent {
    public Player getPlayer() { return null; }
    public Check getCheck() { return new Check(); }
    public int getViolations() { return 0; }
    public String getDebug() { return ""; }

    public static class Check {
        public String getName() { return ""; }
        public String getSubCategory() { return ""; }
        public String getDesc() { return ""; }
    }
}
