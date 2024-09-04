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
    Player player = (Player) sender;
    if (!sender.hasPermission("bungeealerts.use"))
      return false;
    if (args.length == 1) {
      String playerName = args[0];
      List<String> logs = DB.get(playerName);
      int page = 0;
      int pageSize = 10;

      if (logs.isEmpty()) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            BungeeAlerts.getInstance().getConfig().getString("aclogs.not-found")
                .replaceAll("%player%", playerName)));
        return true;
      }

      player.sendMessage(ChatColor.translateAlternateColorCodes('&',
          BungeeAlerts.getInstance().getConfig().getString("aclogs.paginated")
              .replaceAll("%player%", playerName)
              .replaceAll("%page%", String.valueOf(page + 1))
              .replaceAll("%total_pages%", String.valueOf((int) Math.ceil(logs.size() / (double) pageSize)))));

      for (int i = page * pageSize; i < Math.min((page + 1) * pageSize, logs.size()); i++) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', logs.get(i)));
      }

      if (logs.size() > pageSize) {
        page++;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            BungeeAlerts.getInstance().getConfig().getString("aclogs.next-page")
                .replaceAll("%page%", String.valueOf(page + 1))
                .replaceAll("%total_pages%", String.valueOf((int) Math.ceil(logs.size() / (double) pageSize)))));
      } else {
        if (logs.size() > 10) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&',
              BungeeAlerts.getInstance().getConfig().getString("aclogs.next-page")
                  .replaceAll("%page%", String.valueOf(page + 1))
                  .replaceAll("%total_pages%", String.valueOf((int) Math.ceil(logs.size() / (double) pageSize)))));
        }
      }
    } else if (args.length == 2) {
      String playerName = args[0];
      List<String> logs = DB.get(playerName);
      int page = Integer.valueOf(args[1])-1;
      int pageSize = 10;

      if (logs.isEmpty()) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            BungeeAlerts.getInstance().getConfig().getString("aclogs.not-found")
                .replaceAll("%player%", playerName)));
        return true;
      }

      player.sendMessage(ChatColor.translateAlternateColorCodes('&',
          BungeeAlerts.getInstance().getConfig().getString("aclogs.paginated")
              .replaceAll("%player%", playerName)
              .replaceAll("%page%", String.valueOf(page + 1))
              .replaceAll("%total_pages%", String.valueOf((int) Math.ceil(logs.size() / (double) pageSize)))));

      for (int i = page * pageSize; i < Math.min((page + 1) * pageSize, logs.size()); i++) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', logs.get(i)));
      }

      if (logs.size() > pageSize) {
        page++;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            BungeeAlerts.getInstance().getConfig().getString("aclogs.next-page")
                .replaceAll("%page%", String.valueOf(page + 1))
                .replaceAll("%total_pages%", String.valueOf((int) Math.ceil(logs.size() / (double) pageSize)))));
      } else {
        if (logs.size() > 10) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&',
              BungeeAlerts.getInstance().getConfig().getString("aclogs.next-page")
                  .replaceAll("%page%", String.valueOf(page + 1))
                  .replaceAll("%total_pages%", String.valueOf((int) Math.ceil(logs.size() / (double) pageSize)))));
        }
      }
    }
    return true;
  }
}