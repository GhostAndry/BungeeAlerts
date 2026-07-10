package me.ghostdevelopment.bungeealerts.commands;

import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.Settings;
import me.ghostdevelopment.bungeealerts.utils.Check;
import me.ghostdevelopment.bungeealerts.utils.DB;
import me.ghostdevelopment.bungeealerts.utils.DB.AggregatedLog;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Displays anti-cheat violation logs for a player.
 * <p>
 * Usage:
 * <ul>
 *   <li>{@code /aclogs <player>} — aggregated view (grouped by check)</li>
 *   <li>{@code /aclogs <player> -e [page]} — extended view (individual records)</li>
 *   <li>{@code /aclogs <player> --extended [page]} — same as -e</li>
 * </ul>
 * Permission: {@code bungeealerts.use}
 */
public final class CommandACLogs implements CommandExecutor {

    private static final int PAGE_SIZE = Settings.DEFAULT_PAGE_SIZE;
    private static final int MAX_PLAYER_NAME_LENGTH = 16;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = BungeeAlerts.getInstance().getConfig();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString(Settings.CFG_MSG_PLAYER_ONLY,
                            "&cThis command can only be used by players.")));
            return false;
        }

        if (!player.hasPermission("bungeealerts.use")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString(Settings.CFG_MSG_NO_PERMISSION,
                            "&cYou don't have permission to use this command.")));
            return false;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    config.getString(Settings.CFG_MSG_ACLOGS_USAGE,
                            "&cUsage: /aclogs <player> [-e|--extended] [page]")));
            return false;
        }

        String playerName = sanitizeName(args[0]);

        // Check for extended mode flag
        boolean extended = false;
        int pageArgIndex = 1;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("-e") || args[1].equalsIgnoreCase("--extended")) {
                extended = true;
                pageArgIndex = 2;
            }
        }

        if (extended) {
            return showExtended(player, playerName, args, pageArgIndex);
        } else {
            return showAggregated(player, playerName);
        }
    }

    // ── Aggregated view ───────────────────────────────────────

    private boolean showAggregated(Player player, String playerName) {
        FileConfiguration config = BungeeAlerts.getInstance().getConfig();
        List<AggregatedLog> logs = DB.getAggregatedLogs(playerName);

        if (logs.isEmpty()) {
            String notFound = config.getString(Settings.CFG_ACLOGS_NOT_FOUND,
                    "&cNo logs found for %player%.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    notFound.replace(Settings.PH_PLAYER, playerName)));
            return true;
        }

        // Header
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&dAC Logs for &e" + playerName + " &d(aggregated) &7— " + logs.size() + " checks"));

        // Aggregated entries
        String msgFormat = config.getString(Settings.CFG_ACLOGS_MESSAGE,
                "&7(%time%) &d%player% &7> &d%check% &8(&d%vl%&8) &8[&d%server%&8] &7x%count%");
        String hoverFormat = config.getString(Settings.CFG_ACLOGS_HOVER, "");

        for (AggregatedLog log : logs) {
            sendAggregatedMessage(player, log, msgFormat, hoverFormat);
        }

        // Hint for extended view
        TextComponent hint = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                "&7[Extended view: /aclogs " + playerName + " -e]"));
        hint.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/aclogs " + playerName + " -e"));
        player.spigot().sendMessage(hint);

        return true;
    }

    private void sendAggregatedMessage(Player player, AggregatedLog log,
                                        String msgFormat, String hoverFormat) {
        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                msgFormat
                        .replace(Settings.PH_PLAYER, player.getName())
                        .replace(Settings.PH_CHECK, log.checkName())
                        .replace(Settings.PH_VL, String.valueOf(log.maxVl()))
                        .replace(Settings.PH_SERVER, log.server())
                        .replace(Settings.PH_TIME, log.latestTime() != null ? log.latestTime() : "N/A")
                        .replace(Settings.PH_COUNT, String.valueOf(log.totalCount()))
                        .replace(Settings.PH_DESCRIPTION, log.description() != null ? log.description() : "N/A")
                        .replace(Settings.PH_CHECK_INFO, log.checkInfo() != null ? log.checkInfo() : "N/A"));

        String hoverText = ChatColor.translateAlternateColorCodes('&',
                hoverFormat
                        .replace(Settings.PH_PLAYER, player.getName())
                        .replace(Settings.PH_CHECK, log.checkName())
                        .replace(Settings.PH_VL, String.valueOf(log.maxVl()))
                        .replace(Settings.PH_SERVER, log.server())
                        .replace(Settings.PH_TIME, log.latestTime() != null ? log.latestTime() : "N/A")
                        .replace(Settings.PH_COUNT, String.valueOf(log.totalCount()))
                        .replace(Settings.PH_DESCRIPTION, log.description() != null ? log.description() : "N/A")
                        .replace(Settings.PH_CHECK_INFO, log.checkInfo() != null ? log.checkInfo() : "N/A"));

        TextComponent component = new TextComponent(formattedMessage);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hoverText).create()));
        player.spigot().sendMessage(component);
    }

    // ── Extended view ─────────────────────────────────────────

    private boolean showExtended(Player player, String playerName, String[] args, int pageArgIndex) {
        FileConfiguration config = BungeeAlerts.getInstance().getConfig();
        int page = parsePage(args, pageArgIndex);

        int totalLogs = DB.getLogCount(playerName);
        if (totalLogs == 0) {
            String notFound = config.getString(Settings.CFG_ACLOGS_NOT_FOUND,
                    "&cNo logs found for %player%.");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    notFound.replace(Settings.PH_PLAYER, playerName)));
            return true;
        }

        int totalPages = (int) Math.ceil((double) totalLogs / PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        List<Check> pageLogs = DB.getChecksPage(playerName, page, PAGE_SIZE);

        // Header
        String header = config.getString(Settings.CFG_ACLOGS_PAGINATED,
                "&dAC Logs for %player% (Page %page% of %total_pages%) [extended].");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                Settings.formatPaginated(header, playerName, page, totalPages)));

        // Individual entries
        String msgFormat = config.getString(Settings.CFG_ACLOGS_MESSAGE, "");
        String hoverFormat = config.getString(Settings.CFG_ACLOGS_HOVER, "");
        for (Check log : pageLogs) {
            sendCheckMessage(player, log, msgFormat, hoverFormat);
        }

        // Next page
        if (page < totalPages) {
            String nextFmt = config.getString(Settings.CFG_ACLOGS_NEXT_PAGE,
                    "Next page... (> Page %page% of %total_pages%)");
            TextComponent nextPage = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    Settings.formatPaginated(nextFmt, playerName, page + 1, totalPages)));

            nextPage.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/aclogs " + playerName + " -e " + (page + 1)));

            player.spigot().sendMessage(nextPage);
        }

        return true;
    }

    // ── Internal ──────────────────────────────────────────────

    private static String sanitizeName(String name) {
        return name.length() > MAX_PLAYER_NAME_LENGTH
                ? name.substring(0, MAX_PLAYER_NAME_LENGTH)
                : name;
    }

    private static int parsePage(String[] args, int index) {
        if (args.length <= index) return 1;
        try {
            return Math.max(1, Integer.parseInt(args[index]));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private static void sendCheckMessage(Player player, Check check,
                                         String msgFormat, String hoverFormat) {
        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                Settings.format(msgFormat, check));
        String hoverText = ChatColor.translateAlternateColorCodes('&',
                Settings.format(hoverFormat, check));

        TextComponent component = new TextComponent(formattedMessage);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hoverText).create()));
        player.spigot().sendMessage(component);
    }
}
