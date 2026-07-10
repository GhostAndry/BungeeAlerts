package me.ghostdevelopment.bungeealerts.utils;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.Settings;
import me.ghostdevelopment.bungeealerts.Settings.StorageMethod;
import me.ghostdevelopment.bungeealerts.utils.database.*;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Static facade for the AC log persistence layer.
 * <p>
 * Every flag is written directly to the database as a separate record.
 * Aggregation (grouping by check, summing counts) happens at read time
 * via {@link #getAggregatedLogs(String)}.
 */
public final class DB {

    private static DatabaseHandler handler;
    private static final Logger logger = Bukkit.getLogger();

    private DB() {}

    // ── Lifecycle ─────────────────────────────────────────────

    public static void init() {
        String raw = BungeeAlerts.getInstance().getConfig()
                .getString(Settings.CFG_ACLOGS_STORAGE, "mysql");
        StorageMethod method = StorageMethod.fromConfig(raw);

        handler = switch (method) {
            case POSTGRESQL -> new PostgreSQLHandler();
            case MONGODB    -> new MongoDBHandler();
            case SQLITE     -> new SQLiteHandler();
            case MYSQL      -> new MySQLHandler();
        };

        logger.info("Database handler: " + method.name().toLowerCase());
        handler.init();
    }

    public static void close() {
        if (handler != null) {
            handler.close();
            handler = null;
        }
    }

    // ── Write ─────────────────────────────────────────────────

    /** Persists a single check directly to the database. */
    public static void add(Check check) {
        if (!BungeeAlerts.getInstance().getConfig()
                .getBoolean(Settings.CFG_ACLOGS_ENABLED, true)) return;
        if (handler != null) {
            handler.addLog(check);
        }
    }

    // ── Read (raw) ────────────────────────────────────────────

    /** Returns all stored checks for a player, newest first. */
    public static List<Check> getChecks(String playerName) {
        if (handler != null) return handler.getLogs(playerName);
        return new ArrayList<>();
    }

    /** Returns a single page of checks from the database. */
    public static List<Check> getChecksPage(String playerName, int page, int pageSize) {
        if (handler != null) return handler.getLogsPage(playerName, page, pageSize);
        return new ArrayList<>();
    }

    /** Returns the total number of log entries for a player. */
    public static int getLogCount(String playerName) {
        if (handler != null) return handler.getLogCount(playerName);
        return 0;
    }

    // ── Read (aggregated) ─────────────────────────────────────

    /**
     * An aggregated log entry: one per check type, with total count,
     * latest timestamp, and max violation level.
     */
    public record AggregatedLog(String checkName, int totalCount, int maxVl,
                                 String latestTime, String server,
                                 String description, String checkInfo) {}

    /**
     * Returns logs grouped by check type, with counts summed.
     * Used by {@code /aclogs <player>} (default view).
     */
    public static List<AggregatedLog> getAggregatedLogs(String playerName) {
        List<Check> all = getChecks(playerName);
        if (all.isEmpty()) return new ArrayList<>();

        Map<String, List<Check>> groups = all.stream()
                .collect(Collectors.groupingBy(Check::getCheck, LinkedHashMap::new, Collectors.toList()));

        List<AggregatedLog> result = new ArrayList<>();
        for (List<Check> group : groups.values()) {
            if (group.isEmpty()) continue;
            Check latest = group.getFirst(); // newest first
            int totalCount = group.stream().mapToInt(Check::getCount).sum();
            int maxVl = group.stream().mapToInt(Check::getVl).max().orElse(latest.getVl());
            result.add(new AggregatedLog(
                    latest.getCheck(),
                    totalCount,
                    maxVl,
                    latest.getTime(),
                    latest.getServer(),
                    latest.getDescription(),
                    latest.getInfo()
            ));
        }
        return result;
    }
}
