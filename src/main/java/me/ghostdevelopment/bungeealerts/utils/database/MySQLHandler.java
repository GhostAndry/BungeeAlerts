package me.ghostdevelopment.bungeealerts.utils.database;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.Check;
import org.bukkit.Bukkit;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLHandler implements DatabaseHandler {

    private String connectionUrl;
    private String user;
    private String pass;

    @Override
    public void init() {
        String host = BungeeAlerts.getInstance().getConfig().getString("aclogs.sql.host");
        String port = String.valueOf(BungeeAlerts.getInstance().getConfig().getInt("aclogs.sql.port"));
        String dbName = BungeeAlerts.getInstance().getConfig().getString("aclogs.sql.database");
        boolean ssl = BungeeAlerts.getInstance().getConfig().getBoolean("aclogs.sql.use-ssl");
        this.user = BungeeAlerts.getInstance().getConfig().getString("aclogs.sql.username");
        this.pass = BungeeAlerts.getInstance().getConfig().getString("aclogs.sql.password");

        this.connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=" + ssl + "&autoReconnect=true";

        try (Connection conn = DriverManager.getConnection(connectionUrl, user, pass);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS aclogs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "time VARCHAR(23) NOT NULL, " +
                    "playername VARCHAR(16) NOT NULL, " +
                    "check_value VARCHAR(255) NOT NULL, " +
                    "vl INT NOT NULL, " +
                    "server VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "check_info TEXT)";
            stmt.executeUpdate(sql);
            Bukkit.getLogger().info("✅ [MySQL] Connected and table checked.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addLog(Check check) {
        try (Connection conn = DriverManager.getConnection(connectionUrl, user, pass);
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO aclogs (time, playername, check_value, vl, server, description, check_info) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, check.getTime());
            ps.setString(2, check.getPlayerName());
            ps.setString(3, check.getCheck());
            ps.setInt(4, check.getVl());
            ps.setString(5, check.getServer());
            ps.setString(6, check.getDescription());
            ps.setString(7, check.getInfo());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Check> getLogs(String playerName) {
        List<Check> checks = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(connectionUrl, user, pass);
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM aclogs WHERE playername = ? ORDER BY id DESC")) {
            ps.setString(1, playerName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    checks.add(new Check(
                            rs.getString("time"), rs.getString("playername"), rs.getString("check_value"),
                            rs.getInt("vl"), rs.getString("server"), rs.getString("description"), rs.getString("check_info")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return checks;
    }

    @Override
    public void close() { /* Connection pooling handles itself usually, or strictly close here */ }
}