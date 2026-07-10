package me.ghostdevelopment.bungeealerts;

import me.ghostdevelopment.bungeealerts.utils.Check;

/**
 * Centralized constants for all configuration keys, placeholders,
 * storage backends, and shared formatting logic.
 * <p>
 * Every config path lives here — rename a YAML key once, fix one constant.
 * Placeholder keys are reused across alert messages, hover text, and logs.
 */
public final class Settings {

    private Settings() {
        throw new UnsupportedOperationException("Utility class — do not instantiate");
    }

    // ── Storage backend enum ───────────────────────────────────

    /**
     * Supported database backends for the AC log storage system.
     * Use instead of raw string comparison.
     */
    public enum StorageMethod {
        MYSQL,
        POSTGRESQL,
        MONGODB,
        SQLITE;

        /** Resolve from config string, defaulting to {@link #MYSQL}. */
        public static StorageMethod fromConfig(String value) {
            if (value == null) return MYSQL;
            return switch (value.toLowerCase()) {
                case "postgresql" -> POSTGRESQL;
                case "mongodb"   -> MONGODB;
                case "sqlite"    -> SQLITE;
                default          -> MYSQL;
            };
        }
    }

    // ── Config paths ──────────────────────────────────────────
    public static final String CFG_SERVER_NAME     = "server_name";
    public static final String CFG_ALERT_MESSAGE   = "alert_message";
    public static final String CFG_HOVER_MESSAGE   = "hover_message";

    // Redis
    public static final String CFG_REDIS_HOST      = "redis.host";
    public static final String CFG_REDIS_PORT      = "redis.port";
    public static final String CFG_REDIS_PASSWORD  = "redis.password";
    public static final String CFG_REDIS_CHANNEL   = "redis.channel";

    // Alert filtering
    public static final String CFG_ALERT_MINIMUM_VL  = "alert-minimum-vl";

    // Command messages
    public static final String CFG_MSG_PLAYER_ONLY      = "messages.player-only";
    public static final String CFG_MSG_NO_PERMISSION    = "messages.no-permission";
    public static final String CFG_MSG_ALERTS_ENABLED   = "messages.alerts-enabled";
    public static final String CFG_MSG_ALERTS_DISABLED  = "messages.alerts-disabled";
    public static final String CFG_MSG_CONFIG_RELOADED  = "messages.config-reloaded";
    public static final String CFG_MSG_TEST_SENT         = "messages.test-sent";
    public static final String CFG_MSG_DISPATCHER_NOT_READY = "messages.dispatcher-not-ready";
    public static final String CFG_MSG_ACLOGS_USAGE     = "messages.aclogs-usage";

    // ACLogs
    public static final String CFG_ACLOGS_ENABLED             = "aclogs.enabled";
    public static final String CFG_ACLOGS_STORAGE             = "aclogs.storage-method";
    public static final String CFG_ACLOGS_MESSAGE             = "aclogs.message";
    public static final String CFG_ACLOGS_HOVER               = "aclogs.hover_message";
    public static final String CFG_ACLOGS_PAGINATED           = "aclogs.paginated";
    public static final String CFG_ACLOGS_NEXT_PAGE           = "aclogs.next-page";
    public static final String CFG_ACLOGS_NOT_FOUND           = "aclogs.not-found";

    // SQL
    public static final String CFG_SQL_HOST     = "aclogs.sql.host";
    public static final String CFG_SQL_PORT     = "aclogs.sql.port";
    public static final String CFG_SQL_DATABASE = "aclogs.sql.database";
    public static final String CFG_SQL_USERNAME = "aclogs.sql.username";
    public static final String CFG_SQL_PASSWORD = "aclogs.sql.password";
    public static final String CFG_SQL_USE_SSL  = "aclogs.sql.use-ssl";

    // MongoDB
    public static final String CFG_MONGO_URI        = "aclogs.mongodb.uri";
    public static final String CFG_MONGO_DATABASE   = "aclogs.mongodb.database";
    public static final String CFG_MONGO_COLLECTION = "aclogs.mongodb.collection";

    // SQLite
    public static final String CFG_SQLITE_FILE = "aclogs.sqlite.file";

    // ── Placeholder keys ──────────────────────────────────────
    public static final String PH_PLAYER      = "%player%";
    public static final String PH_CHECK       = "%check%";
    public static final String PH_VL          = "%vl%";
    public static final String PH_SERVER      = "%server%";
    public static final String PH_TIME        = "%time%";
    public static final String PH_DESCRIPTION = "%description%";
    public static final String PH_CHECK_INFO  = "%check_info%";
    public static final String PH_COUNT       = "%count%";
    public static final String PH_PAGE        = "%page%";
    public static final String PH_TOTAL_PAGES = "%total_pages%";

    // ── Defaults ──────────────────────────────────────────────
    public static final String DEFAULT_SERVER_NAME    = "server";
    public static final String DEFAULT_REDIS_HOST     = "localhost";
    public static final int    DEFAULT_REDIS_PORT     = 6379;
    public static final String DEFAULT_REDIS_CHANNEL  = "bungeealerts";
    public static final int    DEFAULT_PAGE_SIZE             = 10;

    // ── Utility ───────────────────────────────────────────────

    /**
     * Replaces all standard placeholders in a template string using
     * values from the given {@link Check} object.
     *
     * @param template the message template containing {@code %player%} etc.
     * @param check    the check data to substitute
     * @return the formatted string, or empty string if template or check is null
     */
    public static String format(String template, Check check) {
        if (template == null || check == null) return "";
        return template
                .replace(PH_PLAYER,      check.getPlayerName())
                .replace(PH_CHECK,       check.getCheck())
                .replace(PH_VL,          String.valueOf(check.getVl()))
                .replace(PH_SERVER,      check.getServer())
                .replace(PH_TIME,        check.getTime() != null ? check.getTime() : "N/A")
                .replace(PH_DESCRIPTION, check.getDescription() != null ? check.getDescription() : "N/A")
                .replace(PH_CHECK_INFO,  check.getInfo() != null ? check.getInfo() : "N/A")
                .replace(PH_COUNT,       String.valueOf(check.getCount()));
    }

    /**
     * Formats a pagination template with page numbers.
     *
     * @param template   the template containing {@code %page%} and {@code %total_pages%}
     * @param playerName player name for {@code %player%} substitution
     * @param page       current page number
     * @param totalPages total page count
     * @return the formatted string
     */
    public static String formatPaginated(String template, String playerName,
                                         int page, int totalPages) {
        if (template == null) return "";
        return template
                .replace(PH_PLAYER,      playerName)
                .replace(PH_PAGE,        String.valueOf(page))
                .replace(PH_TOTAL_PAGES, String.valueOf(totalPages));
    }

    /**
     * Strips Minecraft color codes ({@code &0-9a-fk-or}) from a string.
     * Used for clean console logging.
     *
     * @param str the string to strip
     * @return the cleaned string, or empty string if input is null
     */
    public static String stripColors(String str) {
        if (str == null) return "";
        return str.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }
}
