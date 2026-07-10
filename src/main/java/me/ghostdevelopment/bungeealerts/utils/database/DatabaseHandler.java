package me.ghostdevelopment.bungeealerts.utils.database;

import me.ghostdevelopment.bungeealerts.utils.Check;

import java.util.List;

/**
 * Contract for AC log storage backends.
 * <p>
 * Implementations exist for MySQL, PostgreSQL, MongoDB, and SQLite.
 * Each handler manages its own connection lifecycle.
 */
public interface DatabaseHandler {

    /** Initialize the connection and ensure the schema exists. */
    void init();

    /** Persist a single violation log entry. */
    void addLog(Check check);

    /**
     * Retrieve all stored logs for a player, ordered newest-first.
     *
     * @param playerName the target player
     * @return the list of checks (empty if none found)
     */
    List<Check> getLogs(String playerName);

    /**
     * Retrieve a single page of logs for a player.
     *
     * @param playerName the target player
     * @param page       1-based page number
     * @param pageSize   entries per page
     * @return the checks for the requested page (empty if out of bounds)
     */
    List<Check> getLogsPage(String playerName, int page, int pageSize);

    /**
     * Count total log entries for a player.
     *
     * @param playerName the target player
     * @return total number of stored checks
     */
    int getLogCount(String playerName);

    /** Release all resources (connections, clients). */
    void close();
}
