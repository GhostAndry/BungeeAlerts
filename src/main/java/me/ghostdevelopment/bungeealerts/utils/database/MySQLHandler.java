package me.ghostdevelopment.bungeealerts.utils.database;

import com.zaxxer.hikari.HikariConfig;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.Settings;

/**
 * MySQL / MariaDB storage backend for AC violation logs.
 * <p>
 * Uses HikariCP connection pooling. All CRUD and pagination logic
 * is inherited from {@link AbstractSQLHandler}.
 */
public final class MySQLHandler extends AbstractSQLHandler {

    @Override
    protected String getDriverClassName() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    protected String buildJdbcUrl(String host, int port, String dbName) {
        boolean ssl = BungeeAlerts.getInstance().getConfig()
                .getBoolean(Settings.CFG_SQL_USE_SSL, false);
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=" + ssl
                + "&allowPublicKeyRetrieval=true"
                + "&useUnicode=true&characterEncoding=UTF-8";
    }

    @Override
    protected int getDefaultPort() {
        return 3306;
    }

    @Override
    protected String getCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS aclogs ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
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
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
    }
}
