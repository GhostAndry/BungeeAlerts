package me.ghostdevelopment.bungeealerts;

import java.util.ArrayList;
import me.ghostdevelopment.bungeealerts.commands.CommandACLogs;
import me.ghostdevelopment.bungeealerts.commands.CommandBAlerts;
import me.ghostdevelopment.bungeealerts.events.onGrimFlagEvent;
import me.ghostdevelopment.bungeealerts.events.onMatrixFlagEvent;
import me.ghostdevelopment.bungeealerts.events.onVulcanFlagEvent;
import me.ghostdevelopment.bungeealerts.listeners.BungeeMessage;
import me.ghostdevelopment.bungeealerts.utils.DB;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeAlerts extends JavaPlugin {
  private static BungeeAlerts instance;

  public static BungeeAlerts getInstance() {
    return instance;
  }

  public static void setInstance(BungeeAlerts instance) {
    BungeeAlerts.instance = instance;
  }

  public static ArrayList<Player> getStaffer() {
    return staffer;
  }

  private static final ArrayList<Player> staffer = new ArrayList<Player>();

  public void onEnable() {
    setInstance(this);
    getConfig().options().copyDefaults(true);
    saveDefaultConfig();
    getServer().getMessenger().registerIncomingPluginChannel((Plugin) this, "BungeeCord",
        (PluginMessageListener) new BungeeMessage());
    getServer().getMessenger().registerOutgoingPluginChannel((Plugin) this, "BungeeCord");
    registerEvents();
    registerCommands();
    if (getConfig().getBoolean("aclogs.enabled"))
      DB.init();
  }

  public void onDisable() {
    getServer().getMessenger().unregisterOutgoingPluginChannel((Plugin) this, "BungeeCord");
    getServer().getMessenger().unregisterIncomingPluginChannel((Plugin) this, "BungeeCord");
  }

  void registerEvents() {
    PluginManager pm = Bukkit.getPluginManager();
    pm.registerEvents((Listener) new onVulcanFlagEvent(this), (Plugin) this);
    pm.registerEvents((Listener) new onMatrixFlagEvent(this), (Plugin) this);
    pm.registerEvents((Listener) new onGrimFlagEvent(this), (Plugin) this);
  }

  void registerCommands() {
    getCommand("bungeealerts").setExecutor((CommandExecutor) new CommandBAlerts());
    getCommand("aclogs").setExecutor((CommandExecutor) new CommandACLogs());
  }
}