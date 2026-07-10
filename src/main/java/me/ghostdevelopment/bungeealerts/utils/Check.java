package me.ghostdevelopment.bungeealerts.utils;

import java.util.Objects;

/**
 * Immutable data object representing a single anti-cheat flag event.
 * <p>
 * Serialized to JSON for Redis cross-server broadcast and persisted
 * to MySQL / PostgreSQL / MongoDB / SQLite via {@link me.ghostdevelopment.bungeealerts.utils.DB}.
 * <p>
 * The {@code count} field enables aggregation: rapid flags of the same
 * check from the same player within a configurable window increment
 * the count instead of creating duplicate rows.
 * <p>
 * All fields are final — create a new instance for each event.
 */
public final class Check {

    private final String time;
    private final String playerName;
    private final String check;
    private final int vl;
    private final String server;
    private final String description;
    private final String info;
    private final int count;

    /**
     * Full constructor.
     *
     * @param time        timestamp string (microsecond precision)
     * @param playerName  the flagged player's name
     * @param check       anti-cheat check name (e.g. "SPEED_A")
     * @param vl          violation level
     * @param server      server name where the flag occurred
     * @param description human-readable summary
     * @param info        detailed debug information for hover text
     * @param count       number of aggregated flags (1 for new, >1 for aggregated)
     * @throws NullPointerException if time, playerName, check, or server is null
     */
    public Check(String time, String playerName, String check, int vl,
                 String server, String description, String info, int count) {
        this.time = Objects.requireNonNull(time, "time must not be null");
        this.playerName = Objects.requireNonNull(playerName, "playerName must not be null");
        this.check = Objects.requireNonNull(check, "check must not be null");
        this.vl = vl;
        this.server = Objects.requireNonNull(server, "server must not be null");
        this.description = description;
        this.info = info;
        this.count = Math.max(1, count);
    }

    /**
     * Convenience constructor — count defaults to 1.
     */
    public Check(String time, String playerName, String check, int vl,
                 String server, String description, String info) {
        this(time, playerName, check, vl, server, description, info, 1);
    }

    // ── Getters ──────────────────────────────────────────────

    public String getTime()        { return time; }
    public String getPlayerName()  { return playerName; }
    public String getCheck()       { return check; }
    public int    getVl()          { return vl; }
    public String getServer()      { return server; }
    public String getDescription() { return description; }
    public String getInfo()        { return info; }
    public int    getCount()       { return count; }

    // ── Object contract ───────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Check other)) return false;
        return vl == other.vl
                && count == other.count
                && time.equals(other.time)
                && playerName.equals(other.playerName)
                && check.equals(other.check)
                && server.equals(other.server)
                && Objects.equals(description, other.description)
                && Objects.equals(info, other.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, playerName, check, vl, server, description, info, count);
    }

    @Override
    public String toString() {
        return "Check{player=" + playerName + ", check=" + check
                + ", vl=" + vl + ", count=" + count
                + ", server=" + server + ", time=" + time + "}";
    }
}
