package me.ghostdevelopment.bungeealerts.events;

import ac.grim.grimac.api.events.FlagEvent;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.DB;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class onGrimFlagEvent implements Listener {
  BungeeAlerts plugin;
  
  public onGrimFlagEvent(BungeeAlerts plugin) {
    this.plugin = plugin;
  }
  
  @EventHandler
  public void onFlagEvent(FlagEvent event) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    String server_name = this.plugin.getConfig().getString("server_name");
    String flag = this.plugin.getConfig().getString("alert_message")
      .replaceAll("%player%", event.getPlayer().getName())
      .replaceAll("%check%", event.getCheck().getCheckName().toUpperCase())
      .replaceAll("%vl%", String.valueOf(event.getViolations()))
      .replaceAll("%server%", server_name);
    flag = ChatColor.translateAlternateColorCodes('&', flag);
    String consoleFlag = removeCharAndNext(flag);
    Bukkit.getLogger().info(consoleFlag);
    out.writeUTF("Forward");
    out.writeUTF("ALL");
    out.writeUTF("ACALERTS");
    out.writeUTF(flag);
    Player p = (Player)Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
    p.sendPluginMessage((Plugin)this.plugin, "BungeeCord", out.toByteArray());
    for (Player staffer : BungeeAlerts.getStaffer())
      staffer.sendMessage(flag); 
    DB.add(event.getPlayer().getName(), 
        event.getCheck().getCheckName().toUpperCase(), 
        (int)event.getViolations(), 
        server_name);
  }
  
  public static String removeCharAndNext(String str) {
    return str.replaceAll("&.", "");
  }
}
