package me.ghostdevelopment.bungeealerts.commands;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.Check;
import me.ghostdevelopment.bungeealerts.utils.DB;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandACLogs implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bungeealerts.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /aclogs <player> [page]");
            return false;
        }

        String playerName = args[0];
        int page = 1; // Default to first page
        int pageSize = 10;

        // Parse page number if provided
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid page number. Using page 1.");
                page = 1;
            }
        }

        List<Check> logs = DB.getChecksPage(playerName, page, pageSize);

        if (logs.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    BungeeAlerts.getInstance().getConfig().getString("aclogs.not-found")
                            .replace("%player%", playerName)));
            return true;
        }

        // Calculate pagination values
        int totalPages = (int) Math.ceil((double) logs.size() / pageSize);
        page = Math.min(page, totalPages); // Ensure page doesn't exceed total
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, logs.size());

        // Send pagination header
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                BungeeAlerts.getInstance().getConfig().getString("aclogs.paginated")
                        .replace("%player%", playerName)
                        .replace("%page%", String.valueOf(page))
                        .replace("%total_pages%", String.valueOf(totalPages))));

        // Send logs with hover messages
        for (int i = startIndex; i < endIndex; i++) {
            sendCheckMessage(player, logs.get(i));
        }

        // Send next page hint if applicable
        if (page < totalPages) {
            TextComponent nextPage = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    BungeeAlerts.getInstance().getConfig().getString("aclogs.next-page")
                            .replace("%page%", String.valueOf(page + 1))
                            .replace("%total_pages%", String.valueOf(totalPages))));

            // Make clickable to show next page
            nextPage.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/aclogs " + playerName + " " + (page + 1)
            ));

            player.spigot().sendMessage(nextPage);
        }

        return true;
    }

    private void sendCheckMessage(Player player, Check check) {
        // Format the base message
        String messageFormat = BungeeAlerts.getInstance().getConfig().getString("aclogs.message");
        String formattedMessage = messageFormat
                .replace("%time%", check.getTime())
                .replace("%player%", check.getPlayerName())
                .replace("%check%", check.getCheck())
                .replace("%vl%", String.valueOf(check.getVl()))
                .replace("%server%", check.getServer());

        // Create hover text
        String hoverText = BungeeAlerts.getInstance().getConfig().getString("aclogs.hover_message")
                .replace("%time%", check.getTime())
                .replace("%player%", check.getPlayerName())
                .replace("%check%", check.getCheck())
                .replace("%vl%", String.valueOf(check.getVl()))
                .replace("%server%", check.getServer())
                .replace("%description%", check.getDescription())
                .replace("%check_info%", check.getInfo());

        // Create interactive component
        TextComponent component = new TextComponent(
                ChatColor.translateAlternateColorCodes('&', formattedMessage));

        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hoverText)).create()
        ));

        player.spigot().sendMessage(component);
    }
}