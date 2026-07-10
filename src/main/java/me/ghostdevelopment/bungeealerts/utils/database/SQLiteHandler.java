package me.ghostdevelopment.bungeealerts.utils.database;

import com.zaxxer.hikari.HikariConfig;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.Settings;

import java.io.File;

/**
 * SQLite storage backend for AC violation logs.
 * <p>
 * Stores the database file inside the plugin's data folder.
 * Uses HikariCP for consistency with other handlers, though
 * SQLite is single-writer by design.
 * <p>
 * Config key: {@code aclogs.sqlite.file} (default: {@code aclogs.db}).
 */
public final class SQLiteHandler extends AbstractSQLHandler {

    @Override
    protected String getDriverClassName() {
        return "org.sqlite.JDBC";
    }

    @Override
    protected String buildJdbcUrl(String host, int port, String dbName) {
        // host/port/dbName are ignored — SQLite uses a local file
        String fileName = BungeeAlerts.getInstance().getConfig()
                .getString(Settings.CFG_SQLITE_FILE, "aclogs.db");
        File dataFolder = BungeeAlerts.getInstance().getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();
        File dbFile = new File(dataFolder, fileName);
        return "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    @Override
    protected int getDefaultPort() {
        return 0; // not used
    }

    @Override
    protected String getCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS aclogs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "time VARCHAR(26) NOT NULL, "
                + "playername VARCHAR(16) NOT NULL, "
                + "check_value VARCHAR(255) NOT NULL, "
                + "vl INT NOT NULL, "
                + "server VARCHAR(255) NOT NULL, "
                + "description TEXT, "
                + "check_info TEXT, "
                + "count INT NOT NULL DEFAULT 1)";
    }

    @Override
    protected void configurePool(HikariConfig config) {
        // SQLite only supports a single connection
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
    }
}
