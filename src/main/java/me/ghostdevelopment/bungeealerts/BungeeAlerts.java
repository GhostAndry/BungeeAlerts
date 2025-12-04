package me.ghostdevelopment.bungeealerts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Getter;
import me.ghostdevelopment.bungeealerts.commands.CommandACLogs;
import me.ghostdevelopment.bungeealerts.commands.CommandBAlerts;
import me.ghostdevelopment.bungeealerts.commands.CommandTestalert;
import me.ghostdevelopment.bungeealerts.events.*;
import me.ghostdevelopment.bungeealerts.redis.RedisManager;
import me.ghostdevelopment.bungeealerts.utils.DB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeAlerts extends JavaPlugin {
    
    
    private static BungeeAlerts instance;
    private static RedisManager redisManager;

    private static final HashMap<UUID, Boolean> staffer = new HashMap<>();

    public static HashMap<UUID, Boolean> getStafferMap() {
        return staffer;
    }

    public static Collection<Player> getStaffer() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> staffer.getOrDefault(p.getUniqueId(), false))
                .collect(Collectors.toList());
    }

    public static Object getInstance() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static RedisManager getRedisManager() {
        return redisManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        redisManager = new RedisManager(this);
        redisManager.sendMessage("§aBungeeAlerts has been enabled on " + getConfig().getString("server_name") + "!");

        registerEvents();
        registerCommands();

        redisManager.startListening();
        
        if (getConfig().getBoolean("aclogs.enabled")) DB.init();
    }

    void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new onJoinEvent(), this);
        pm.registerEvents(new onVulcanFlagEvent(this), this);
        pm.registerEvents(new onMatrixFlagEvent(this), this);
        pm.registerEvents(new onGrimFlagEvent(this), this);
        try {
            if( Bukkit.getPluginManager().getPlugin("KarhuAPI") == null
                    || Bukkit.getPluginManager().getPlugin("KarhuAC") == null
                    || Bukkit.getPluginManager().getPlugin("Karhu") == null
            ) return;
            onKarhuFlagEvent.register(this);
        }catch (Exception e) {
            getLogger().warning("KarhuAPI not found, skipping Karhu flag event registration.");
        }
    }

    @SuppressWarnings("all")
    void registerCommands() {
        getCommand("bungeealerts").setExecutor(new CommandBAlerts());
        getCommand("aclogs").setExecutor(new CommandACLogs());
        getCommand("testalert").setExecutor(new CommandTestalert());
    }
}