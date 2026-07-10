package me.ghostdevelopment.bungeealerts.commands;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.Settings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Toggles anti-cheat alerts on/off or reloads configuration.
 * <p>
 * Usage:
 * <ul>
 *   <li>{@code /bungeealerts} — toggle alerts on/off</li>
 *   <li>{@code /bungeealerts reload} — reload config.yml (permission: {@code bungeealerts.reload})</li>
 * </ul>
 */
public final class CommandBAlerts implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = BungeeAlerts.getInstance().getConfig();

        // Reload subcommand
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            return handleReload(sender, config);
        }

        // Toggle alerts (player only)
        if (!(sender instanceof Player player)) {
            sender.sendMessage(color(config.getString(Settings.CFG_MSG_PLAYER_ONLY,
                    "&cThis command can only be used by players.")));
            return false;
        }

        if (!player.hasPermission("bungeealerts.use")) {
            player.sendMessage(color(config.getString(Settings.CFG_MSG_NO_PERMISSION,
                    "&cYou don't have permission to use this command.")));
            return false;
        }

        UUID uuid = player.getUniqueId();
        Set<UUID> staffSet = BungeeAlerts.getStaffSet();

        if (staffSet.remove(uuid)) {
            player.sendMessage(color(config.getString(Settings.CFG_MSG_ALERTS_DISABLED,
                    "&cAlerts disabled.")));
        } else {
            staffSet.add(uuid);
            player.sendMessage(color(config.getString(Settings.CFG_MSG_ALERTS_ENABLED,
                    "&aAlerts enabled.")));
        }

        return true;
    }

    private boolean handleReload(CommandSender sender, FileConfiguration config) {
        if (!sender.hasPermission("bungeealerts.reload")) {
            sender.sendMessage(color(config.getString(Settings.CFG_MSG_NO_PERMISSION,
                    "&cYou don't have permission to use this command.")));
            return false;
        }

        BungeeAlerts plugin = BungeeAlerts.getInstance();
        plugin.reloadConfig();
        plugin.getConfig().options().copyDefaults(true);
        sender.sendMessage(color(config.getString(Settings.CFG_MSG_CONFIG_RELOADED,
                "&aBungeeAlerts configuration reloaded.")));
        return true;
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
