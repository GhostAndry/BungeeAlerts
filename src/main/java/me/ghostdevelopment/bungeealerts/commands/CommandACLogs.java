package me.ghostdevelopment.bungeealerts.commands;

import java.util.List;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.DB;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandACLogs implements CommandExecutor {
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player))
      return false; 
    Player player = (Player)sender;
    if (!sender.hasPermission("bungeealerts.use"))
      return false; 
    if (args.length == 1) {
      String playerName = args[0];
      List<String> logs = DB.get(playerName);
      if (logs.isEmpty())
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', BungeeAlerts.getInstance().getConfig().getString("aclogs.not-found")
              .replaceAll("%player%", playerName))); 
      for (String message : logs)
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message)); 
    } 
    return true;
  }
}
