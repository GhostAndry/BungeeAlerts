package me.ghostdevelopment.bungeealerts.utils.database;

/**
 * PostgreSQL storage backend for AC violation logs.
 * <p>
 * Uses HikariCP connection pooling. All CRUD and pagination logic
 * is inherited from {@link AbstractSQLHandler}.
 * <p>
 * Differs from MySQL only in the JDBC URL format, default port (5432),
 * and {@code SERIAL} instead of {@code INT AUTO_INCREMENT}.
 */
public final class PostgreSQLHandler extends AbstractSQLHandler {

    @Override
    protected String getDriverClassName() {
        return "org.postgresql.Driver";
    }

    @Override
    protected String buildJdbcUrl(String host, int port, String dbName) {
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    @Override
    protected int getDefaultPort() {
        return 5432;
    }

    @Override
    protected String getCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS aclogs ("
                + "id SERIAL PRIMARY KEY, "
                + "time VARCHAR(26) NOT NULL, "
                + "playername VARCHAR(16) NOT NULL, "
                + "check_value VARCHAR(255) NOT NULL, "
                + "vl INT NOT NULL, "
                + "server VARCHAR(255) NOT NULL, "
                + "description TEXT, "
                + "check_info TEXT, "
                + "count INT NOT NULL DEFAULT 1)";
    }
}
