package me.ghostdevelopment.bungeealerts.utils.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.Settings;
import me.ghostdevelopment.bungeealerts.utils.Check;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for JDBC-based storage backends (MySQL, PostgreSQL, SQLite).
 * <p>
 * Manages a HikariCP connection pool and provides common CRUD operations.
 * Subclasses only need to supply the JDBC driver, URL format, default port,
 * and the CREATE TABLE statement.
 * <p>
 * Pagination is pushed to the database via {@code LIMIT / OFFSET}, so
 * {@link me.ghostdevelopment.bungeealerts.commands.CommandACLogs} no longer
 * fetches every row into memory.
 */
public abstract class AbstractSQLHandler implements DatabaseHandler {

    protected HikariDataSource dataSource;
    protected final Logger logger = Bukkit.getLogger();

    // ── Subclass contract ─────────────────────────────────────

    /** JDBC driver class name, e.g. {@code com.mysql.cj.jdbc.Driver}. */
    protected abstract String getDriverClassName();

    /**
     * Build the JDBC URL from config values.
     *
     * @param host   database host
     * @param port   database port
     * @param dbName database / schema name
     * @return the full JDBC connection URL
     */
    protected abstract String buildJdbcUrl(String host, int port, String dbName);

    /** Default port for this database engine. */
    protected abstract int getDefaultPort();

    /** Default host for this database engine. */
    protected String getDefaultHost() { return "localhost"; }

    /** Default database / schema name. */
    protected String getDefaultDatabase() { return "aclogs"; }

    /**
     * The {@code CREATE TABLE IF NOT EXISTS} statement.
     * Must produce a table named {@code aclogs} with columns:
     * {@code id, time, playername, check_value, vl, server, description, check_info}.
     */
    protected abstract String getCreateTableSql();

    /**
     * Hook for subclasses to set extra HikariConfig properties
     * (e.g. connection timeout, SSL, pool size).
     */
    protected void configurePool(HikariConfig config) {
        // no-op by default
    }

    // ── DatabaseHandler implementation ────────────────────────

    @Override
    public void init() {
        var config = BungeeAlerts.getInstance().getConfig();
        String host = config.getString(Settings.CFG_SQL_HOST, getDefaultHost());
        int port = config.getInt(Settings.CFG_SQL_PORT, getDefaultPort());
        String dbName = config.getString(Settings.CFG_SQL_DATABASE, getDefaultDatabase());
        String user = config.getString(Settings.CFG_SQL_USERNAME, "root");
        String pass = config.getString(Settings.CFG_SQL_PASSWORD, "password");

        HikariConfig hikari = new HikariConfig();
        hikari.setDriverClassName(getDriverClassName());
        hikari.setJdbcUrl(buildJdbcUrl(host, port, dbName));
        hikari.setUsername(user);
        hikari.setPassword(pass);
        hikari.setMaximumPoolSize(5);
        hikari.setMinimumIdle(1);
        hikari.setConnectionTimeout(5000);
        hikari.setIdleTimeout(300000);
        hikari.setMaxLifetime(600000);
        hikari.setPoolName("BungeeAlerts-" + getClass().getSimpleName());
        configurePool(hikari);

        try {
            dataSource = new HikariDataSource(hikari);
            createTable();
            logger.info("[" + getClass().getSimpleName() + "] Pool ready.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[" + getClass().getSimpleName() + "] Pool init failed", e);
        }
    }

    @Override
    public void addLog(Check check) {
        String sql = "INSERT INTO aclogs (time, playername, check_value, vl, server, description, check_info, count)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, check.getTime());
            ps.setString(2, check.getPlayerName());
            ps.setString(3, check.getCheck());
            ps.setInt(4, check.getVl());
            ps.setString(5, check.getServer());
            ps.setString(6, check.getDescription());
            ps.setString(7, check.getInfo());
            ps.setInt(8, check.getCount());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[" + getClass().getSimpleName() + "] Insert failed", e);
        }
    }

    @Override
    public List<Check> getLogs(String playerName) {
        List<Check> checks = new ArrayList<>();
        String sql = "SELECT * FROM aclogs WHERE playername = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    checks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[" + getClass().getSimpleName() + "] Query failed for " + playerName, e);
        }
        return checks;
    }

    /**
     * Paginated log retrieval — only fetches the requested page from the database.
     *
     * @param playerName target player
     * @param page       1-based page number
     * @param pageSize   entries per page
     * @return the checks for the requested page
     */
    public List<Check> getLogsPage(String playerName, int page, int pageSize) {
        List<Check> checks = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql = "SELECT * FROM aclogs WHERE playername = ? ORDER BY id DESC LIMIT ? OFFSET ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerName);
            ps.setInt(2, pageSize);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    checks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[" + getClass().getSimpleName() + "] Paged query failed", e);
        }
        return checks;
    }

    /**
     * Returns the total number of log entries for a player.
     */
    public int getLogCount(String playerName) {
        String sql = "SELECT COUNT(*) FROM aclogs WHERE playername = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[" + getClass().getSimpleName() + "] Count failed", e);
        }
        return 0;
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            logger.info("[" + getClass().getSimpleName() + "] Pool closed.");
        }
    }

    // ── Internal ──────────────────────────────────────────────

    protected Connection getConnection() throws SQLException {
        if (dataSource == null) throw new SQLException("DataSource not initialized");
        return dataSource.getConnection();
    }

    private void createTable() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(getCreateTableSql());
            logger.info("[" + getClass().getSimpleName() + "] Table 'aclogs' ready.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[" + getClass().getSimpleName() + "] Table creation failed", e);
        }
    }

    private static Check mapRow(ResultSet rs) throws SQLException {
        int count = 1;
        try {
            count = rs.getInt("count");
        } catch (SQLException ignored) {
            // Column may not exist in legacy tables
        }
        return new Check(
                rs.getString("time"),
                rs.getString("playername"),
                rs.getString("check_value"),
                rs.getInt("vl"),
                rs.getString("server"),
                rs.getString("description"),
                rs.getString("check_info"),
                count
        );
    }
}
