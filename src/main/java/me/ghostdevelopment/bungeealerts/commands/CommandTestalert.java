package me.ghostdevelopment.bungeealerts.commands;

import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import net.md_5.bungee.api.ChatColor;

public class CommandTestalert implements CommandExecutor {

    public BungeeAlerts plugin;

    public CommandTestalert(BungeeAlerts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("bungeealerts.test")) {
            return false;
        }

        String serverName = this.plugin.getConfig().getString("server_name");
        String flag = this.plugin.getConfig().getString("alert_message")
                .replaceAll("%player%", player.getName())
                .replaceAll("%check%", "".toUpperCase(Locale.ROOT))
                .replaceAll("%vl%", String.valueOf("0"))
                .replaceAll("%server%", serverName);
        flag = ChatColor.translateAlternateColorCodes('&', flag);

        BungeeAlerts.getRedisManager().sendMessage(flag);
        return true;
    }

}
