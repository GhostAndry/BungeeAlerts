package me.ghostdevelopment.bungeealerts;

import lombok.Getter;
import me.ghostdevelopment.bungeealerts.commands.CommandACLogs;
import me.ghostdevelopment.bungeealerts.commands.CommandBAlerts;
import me.ghostdevelopment.bungeealerts.commands.CommandTabCompleter;
import me.ghostdevelopment.bungeealerts.commands.CommandTestalert;
import me.ghostdevelopment.bungeealerts.events.*;
import me.ghostdevelopment.bungeealerts.redis.RedisManager;
import me.ghostdevelopment.bungeealerts.utils.DB;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * BungeeAlerts — Cross-server anti-cheat alert system.
 * <p>
 * Hooks into Vulcan, Matrix, GrimAC, and Karhu flag events,
 * broadcasts alerts to staff via Redis pub/sub, and persists
 * violation logs to MySQL / PostgreSQL / MongoDB / SQLite.
 *
 * <h3>Quick start</h3>
 * <pre>{@code
 *   # Build with AC JARs in lib/
 *   mvn clean package
 *
 *   # Build without AC JARs (uses stubs)
 *   mvn clean package -Pno-libs
 * }</pre>
 */
public final class BungeeAlerts extends JavaPlugin {

    // ── Static state ─────────────────────────────────────────

    /**
     * @return the singleton plugin instance
     */
    @Getter
    private static BungeeAlerts instance;
    /**
     * @return the Redis pub/sub manager, or null if not initialized
     */
    @Getter
    private static RedisManager redisManager;
    /**
     * @return the alert dispatch pipeline
     */
    @Getter
    private static AlertDispatcher dispatcher;

    /**
     * Staff UUIDs that have alerts enabled.
     * {@link ConcurrentHashMap#newKeySet()} gives O(1) add/remove/contains
     * without streaming all online players on every alert.
     * -- GETTER --
     *
     * @return the set of staff UUIDs with alerts toggled on

     */
    @Getter
    private static final Set<UUID> staffSet = ConcurrentHashMap.newKeySet();

    // ── Public accessors ─────────────────────────────────────

    /**
     * Returns online players whose UUID is in the staff set.
     * Called on every alert — keep it lean.
     */
    public static Collection<Player> getStaffer() {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        if (online.isEmpty()) return Collections.emptyList();
        return online.stream()
                .filter(p -> staffSet.contains(p.getUniqueId()))
                .collect(Collectors.toList());
    }

    // ── Lifecycle ────────────────────────────────────────────

    @Override
    public void onEnable() {
        instance = this;
        Logger log = getLogger();

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        dispatcher = new AlertDispatcher(this);

        redisManager = new RedisManager(this);
        redisManager.sendMessage("§aBungeeAlerts has been enabled on "
                + getConfig().getString(Settings.CFG_SERVER_NAME, Settings.DEFAULT_SERVER_NAME) + "!");

        registerEvents();
        registerCommands();

        redisManager.startListening();

        if (getConfig().getBoolean(Settings.CFG_ACLOGS_ENABLED, true)) {
            DB.init();
        }

        // Update checker (async, non-blocking)
        UpdateChecker updateChecker = new UpdateChecker(this);
        updateChecker.check();
        Bukkit.getPluginManager().registerEvents(updateChecker, this);

        log.info("BungeeAlerts v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (redisManager != null) {
            redisManager.shutdown();
        }
        DB.close();
        staffSet.clear();
        getLogger().info("BungeeAlerts disabled. Connections closed.");
    }

    // ── Registration ─────────────────────────────────────────

    private void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new onJoinEvent(), this);
        pm.registerEvents(new onVulcanFlagEvent(this), this);
        pm.registerEvents(new onMatrixFlagEvent(this), this);
        pm.registerEvents(new onGrimFlagEvent(this), this);

        try {
            if (Bukkit.getPluginManager().getPlugin("KarhuAPI") == null
                    && Bukkit.getPluginManager().getPlugin("KarhuAC") == null
                    && Bukkit.getPluginManager().getPlugin("Karhu") == null) {
                getLogger().warning("Karhu not found — skipping Karhu listener registration.");
                return;
            }
            onKarhuFlagEvent.register(this);
        } catch (Exception e) {
            getLogger().warning("KarhuAPI not found, skipping Karhu flag event registration.");
        }
    }

    private void registerCommands() {
        var tabCompleter = new CommandTabCompleter();
        registerCommand("bungeealerts", new CommandBAlerts(), tabCompleter);
        registerCommand("aclogs", new CommandACLogs(), tabCompleter);
        registerCommand("testalert", new CommandTestalert(), tabCompleter);
    }

    private void registerCommand(String name, CommandExecutor executor, TabCompleter tabCompleter) {
        var cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
            cmd.setTabCompleter(tabCompleter);
        } else {
            getLogger().warning("Command '" + name + "' not found in plugin.yml — skipping registration.");
        }
    }
}
