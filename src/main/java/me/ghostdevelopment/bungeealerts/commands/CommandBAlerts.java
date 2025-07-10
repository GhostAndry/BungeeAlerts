package me.ghostdevelopment.bungeealerts.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;

import java.util.UUID;

public class CommandBAlerts implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!player.hasPermission("bungeealerts.use")) return false;

        UUID uuid = player.getUniqueId();
        boolean current = BungeeAlerts.getStafferMap().getOrDefault(uuid, false);

        BungeeAlerts.getStafferMap().put(uuid, !current);
        if (current) {
            player.sendMessage(color("&cAlerts disabled."));
        } else {
            player.sendMessage(color("&aAlerts enabled."));
        }

        return true;
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}