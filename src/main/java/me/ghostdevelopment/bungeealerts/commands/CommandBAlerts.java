package me.ghostdevelopment.bungeealerts.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;

public class CommandBAlerts implements CommandExecutor {
    
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;
        if (!player.hasPermission("bungeealerts.use"))
            return false;
        if (BungeeAlerts.getStaffer().contains(player)) {
            BungeeAlerts.getStaffer().remove(player);
            player.sendMessage(color("&cAlerts disabled."));
        } else {
            BungeeAlerts.getStaffer().add(player);
            player.sendMessage(color("&aAlerts enabled."));
        }
        return true;
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
