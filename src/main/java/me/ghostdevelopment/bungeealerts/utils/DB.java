package me.ghostdevelopment.bungeealerts.utils;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.database.*;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.List;

public class DB {

    private static DatabaseHandler handler;

    public static void init() {
        String method = BungeeAlerts.getInstance().getConfig().getString("aclogs.storage-method", "mysql").toLowerCase();

        switch (method) {
            case "postgresql":
                handler = new PostgreSQLHandler();
                break;
            case "mongodb":
                handler = new MongoDBHandler();
                break;
            case "sqlite":
                // Se vuoi implementare SQLite, crea una classe SQLiteHandler simile a MySQLHandler 
                // ma con url "jdbc:sqlite:plugins/BungeeAlerts/aclogs.db"
                // handler = new SQLiteHandler(); 
                Bukkit.getLogger().warning("SQLite implementation pending. Falling back to MySQL logic (may fail if not configured).");
                handler = new MySQLHandler(); 
                break;
            case "mysql":
            default:
                handler = new MySQLHandler();
                break;
        }

        Bukkit.getLogger().info("📡 Initializing database handler: " + method);
        handler.init();
    }

    public static void add(Check check) {
        if (!BungeeAlerts.getInstance().getConfig().getBoolean("aclogs.enabled")) return;
        if (handler != null) {
            handler.addLog(check);
        }
    }

    public static List<Check> getChecks(String playerName) {
        if (handler != null) {
            return handler.getLogs(playerName);
        }
        return new ArrayList<>();
    }

    public static List<Check> getChecksPage(String playerName, int page, int pageSize) {
        List<Check> all = getChecks(playerName);
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, all.size());

        if (fromIndex >= all.size()) return new ArrayList<>();
        return all.subList(fromIndex, toIndex);
    }
    
    public static void close() {
        if (handler != null) handler.close();
    }
}