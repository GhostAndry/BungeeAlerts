package me.ghostdevelopment.bungeealerts.commands;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBAlerts implements CommandExecutor {
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player))
      return false; 
    Player player = (Player)sender;
    if (!player.hasPermission("bungeealerts.use"))
      return false; 
    if (BungeeAlerts.getStaffer().contains(player)) {
      BungeeAlerts.getStaffer().remove(player);
      player.sendMessage(color("&cAll alerts disabled."));
    } else {
      BungeeAlerts.getStaffer().add(player);
      player.sendMessage(color("&aAll alerts enabled."));
    } 
    return true;
  }
  
  private static String color(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }
}
