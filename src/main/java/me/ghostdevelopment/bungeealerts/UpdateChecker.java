package me.ghostdevelopment.bungeealerts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks GitHub Releases for newer versions on startup.
 * <p>
 * Notifies console and staff (on join) if an update is available.
 * Runs asynchronously to avoid blocking the main thread.
 */
public final class UpdateChecker implements Listener {

    private static final String API_URL =
            "https://api.github.com/repos/GhostAndry/BungeeAlerts/releases/latest";
    private static final String RELEASES_URL =
            "https://github.com/GhostAndry/BungeeAlerts/releases";

    private final BungeeAlerts plugin;
    private final Logger logger;
    private String latestVersion;
    private boolean updateAvailable;

    public UpdateChecker(BungeeAlerts plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /** Runs the check on a background thread. */
    public void check() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URI(API_URL).toURL().openConnection();
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setRequestProperty("User-Agent", "BungeeAlerts-UpdateChecker");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() != 200) {
                    logger.warning("Update check failed: HTTP " + conn.getResponseCode());
                    return;
                }

                JsonObject json = JsonParser.parseReader(new InputStreamReader(conn.getInputStream()))
                        .getAsJsonObject();
                latestVersion = json.get("tag_name").getAsString();
                String currentVersion = plugin.getDescription().getVersion();

                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    updateAvailable = true;
                    logger.warning("==============================================");
                    logger.warning("BungeeAlerts update available!");
                    logger.warning("Current: v" + currentVersion);
                    logger.warning("Latest:  " + latestVersion);
                    logger.warning("Download: " + RELEASES_URL);
                    logger.warning("==============================================");
                } else {
                    logger.info("BungeeAlerts is up to date (v" + currentVersion + ").");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Update check failed: " + e.getMessage());
            }
        });
    }

    /** Notify staff when they join if an update is available. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!updateAvailable) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("bungeealerts.use") && !player.isOp()) return;

        player.sendMessage("§8[§5§lBungeeAlerts§8] §eUpdate available: §f" + latestVersion
                + " §7(current: §f" + plugin.getDescription().getVersion() + "§7)");
        player.sendMessage("§8[§5§lBungeeAlerts§8] §7Download: §f" + RELEASES_URL);
    }
}
