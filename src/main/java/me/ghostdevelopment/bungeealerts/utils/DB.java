package me.ghostdevelopment.bungeealerts.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import org.bukkit.Bukkit;

public class DB {
    private static String DB_IP;
    private static String DB_PORT;
    private static String DB_NAME;
    private static String USER;
    private static String PASS;

    public static void init() {
        DB_IP = BungeeAlerts.getInstance().getConfig().getString("aclogs.mysql.host");
        DB_PORT = String.valueOf(BungeeAlerts.getInstance().getConfig().getInt("aclogs.mysql.port"));
        DB_NAME = BungeeAlerts.getInstance().getConfig().getString("aclogs.mysql.database");
        USER = BungeeAlerts.getInstance().getConfig().getString("aclogs.mysql.username");
        PASS = BungeeAlerts.getInstance().getConfig().getString("aclogs.mysql.password");

        String dbUrl = "jdbc:mysql://" + DB_IP + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&serverTimezone=UTC";
        Bukkit.getLogger().info("📡 Connecting to MySQL at: " + dbUrl);

        try (Connection connection = DriverManager.getConnection(dbUrl, USER, PASS);
             Statement statement = connection.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS aclogs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "time VARCHAR(23) NOT NULL, " +
                    "playername VARCHAR(16) NOT NULL, " +
                    "check_value VARCHAR(255) NOT NULL, " +
                    "vl INT NOT NULL, " +
                    "server VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +          // Added description field
                    "check_info TEXT)";             // Hover information

            statement.executeUpdate(sql);
            Bukkit.getLogger().info("✅ Table 'aclogs' created successfully.");

        } catch (Exception e) {
            Bukkit.getLogger().severe("❌ Failed to create database table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getDBUrl() {
        return "jdbc:mysql://" + DB_IP + ":" + DB_PORT + "/" + DB_NAME;
    }

    public static void add(Check check) {
        if (!BungeeAlerts.getInstance().getConfig().getBoolean("aclogs.enabled")) return;

        try (Connection connection = DriverManager.getConnection(getDBUrl(), USER, PASS);
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO aclogs (time, playername, check_value, vl, server, description, check_info) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, check.getTime());
            ps.setString(2, check.getPlayerName());
            ps.setString(3, check.getCheck());
            ps.setInt(4, check.getVl());
            ps.setString(5, check.getServer());
            ps.setString(6, check.getDescription());
            ps.setString(7, check.getInfo());
            ps.executeUpdate();
        } catch (SQLException se) {
            Bukkit.getLogger().severe("❌ Failed to add AC log: " + se.getMessage());
            se.printStackTrace();
        }
    }

    public static List<Check> getChecks(String playerName) {
        List<Check> checks = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(getDBUrl(), USER, PASS);
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT * FROM aclogs WHERE playername = ? ORDER BY id DESC")) {

            ps.setString(1, playerName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    checks.add(new Check(
                            rs.getString("time"),
                            rs.getString("playername"),
                            rs.getString("check_value"),
                            rs.getInt("vl"),
                            rs.getString("server"),
                            rs.getString("description"),  // Load description
                            rs.getString("check_info")    // Load hover info
                    ));
                }
            }
        } catch (SQLException se) {
            Bukkit.getLogger().severe("❌ Failed to get AC logs: " + se.getMessage());
            se.printStackTrace();
        }
        return checks;
    }

    public static List<Check> getChecksPage(String playerName, int page, int pageSize) {
        List<Check> all = getChecks(playerName); // già ordinati DESC
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, all.size());

        if (fromIndex >= all.size()) return new ArrayList<>();
        return all.subList(fromIndex, toIndex);
    }

}