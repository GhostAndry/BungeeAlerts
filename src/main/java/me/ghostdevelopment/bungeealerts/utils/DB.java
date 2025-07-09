package me.ghostdevelopment.bungeealerts.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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

            String sql = "CREATE TABLE IF NOT EXISTS aclogs (id INT AUTO_INCREMENT PRIMARY KEY, time VARCHAR(23), playername VARCHAR(16), check_value VARCHAR(255), vl INT, server VARCHAR(255))";
            statement.executeUpdate(sql);
            Bukkit.getLogger().info("✅ Table 'aclogs' created successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getDBUrl() {
        return "jdbc:mysql://" + DB_IP + ":" + DB_PORT + "/" + DB_NAME;
    }

    public static void add(String playerName, String check, int vl, String server) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            if(!BungeeAlerts.getInstance().getConfig().getBoolean("aclogs.enabled")) return;

            connection = DriverManager.getConnection(getDBUrl(), USER, PASS);
            String sql = "INSERT INTO aclogs (time, playername, check_value, vl, server) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setString(2, playerName);
            preparedStatement.setString(3, check);
            preparedStatement.setInt(4, vl);
            preparedStatement.setString(5, server);
            preparedStatement.executeUpdate();
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static List<String> get(String playerName) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<String> messages = new ArrayList<String>();
        try {
            connection = DriverManager.getConnection(getDBUrl(), USER, PASS);
            String sql = "SELECT * FROM aclogs WHERE playername = ? ORDER BY id DESC";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, playerName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String message = formatMessage(resultSet);
                messages.add(message);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (preparedStatement != null)
                    preparedStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return messages;
    }

    private static String formatMessage(ResultSet resultSet) throws SQLException {
        String time = resultSet.getString("time");
        String playerName = resultSet.getString("playername");
        String check = resultSet.getString("check_value");
        int vl = resultSet.getInt("vl");
        String server = resultSet.getString("server");
        String message = BungeeAlerts.getInstance().getConfig().getString("aclogs.message")
                .replaceAll("%time%", time)
                .replaceAll("%player%", playerName)
                .replaceAll("%check%", check)
                .replaceAll("%vl%", String.valueOf(vl))
                .replaceAll("%server%", server);
        return message;
    }
}
