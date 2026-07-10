package me.liwk.karhu.api;

/** Stub for building without Karhu JAR. */
public class KarhuAPI {
    private static final EventRegistry registry = new EventRegistry();
    public static EventRegistry getEventRegistry() { return registry; }

    public static class EventRegistry {
        public void addListener(Object listener) {}
    }
}
