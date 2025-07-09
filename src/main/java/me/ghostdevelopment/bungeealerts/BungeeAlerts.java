package me.ghostdevelopment.bungeealerts;

import java.util.ArrayList;

import lombok.Getter;
import me.ghostdevelopment.bungeealerts.commands.CommandACLogs;
import me.ghostdevelopment.bungeealerts.commands.CommandBAlerts;
import me.ghostdevelopment.bungeealerts.events.onGrimFlagEvent;
import me.ghostdevelopment.bungeealerts.events.onJoinEvent;
import me.ghostdevelopment.bungeealerts.events.onMatrixFlagEvent;
import me.ghostdevelopment.bungeealerts.events.onVulcanFlagEvent;
import me.ghostdevelopment.bungeealerts.redis.RedisManager;
import me.ghostdevelopment.bungeealerts.utils.DB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BungeeAlerts extends JavaPlugin {
    
    
    private static BungeeAlerts instance;

    private static final ArrayList<Player> staffer = new ArrayList<>();

    private static RedisManager redisManager;

    public static BungeeAlerts getInstance() {
        return instance;
    }

    public static void setInstance(BungeeAlerts instance) {
        BungeeAlerts.instance = instance;
    }

    public static RedisManager getRedisManager() {
        return redisManager;
    }

    public static void setRedisManager(RedisManager redisManager) {
        BungeeAlerts.redisManager = redisManager;
    }

    public static ArrayList<Player> getStaffer() {
        return new ArrayList<>(staffer);
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

        new Thread(()->{
            redisManager.startListening();
        }).start();
        
        if (getConfig().getBoolean("aclogs.enabled")) DB.init();
    }

    void registerEvents() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new onJoinEvent(), this);
        pm.registerEvents(new onVulcanFlagEvent(this), this);
        pm.registerEvents(new onMatrixFlagEvent(this), this);
        pm.registerEvents(new onGrimFlagEvent(this), this);
    }

    @SuppressWarnings("all")
    void registerCommands() {
        getCommand("bungeealerts").setExecutor(new CommandBAlerts());
        getCommand("aclogs").setExecutor(new CommandACLogs());
    }
}