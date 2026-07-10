package me.ghostdevelopment.bungeealerts.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completion for BungeeAlerts commands.
 * <p>
 * {@code /aclogs <player>} — suggests online player names, then {@code -e} / {@code --extended}.
 * <br>
 * {@code /bungeealerts} — suggests {@code reload}.
 * <br>
 * {@code /testalert} — suggests online player names.
 */
public final class CommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        return switch (cmd) {
            case "aclogs"    -> completeAcLogs(args);
            case "bungeealerts", "balerts" -> completeBAlerts(args);
            case "testalert", "talerts"    -> completeTestAlert(args);
            default         -> List.of();
        };
    }

    // ── /aclogs ──────────────────────────────────────────────

    private List<String> completeAcLogs(String[] args) {
        if (args.length == 1) {
            // Suggest online player names
            return matchPlayerNames(args[0]);
        }
        if (args.length == 2) {
            // Suggest -e / --extended
            String partial = args[1].toLowerCase();
            List<String> options = new ArrayList<>();
            if ("-e".startsWith(partial)) options.add("-e");
            if ("--extended".startsWith(partial)) options.add("--extended");
            return options;
        }
        return List.of();
    }

    // ── /bungeealerts ────────────────────────────────────────

    private List<String> completeBAlerts(String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("reload".startsWith(partial)) return List.of("reload");
        }
        return List.of();
    }

    // ── /testalert ───────────────────────────────────────────

    private List<String> completeTestAlert(String[] args) {
        if (args.length == 1) {
            return matchPlayerNames(args[0]);
        }
        return List.of();
    }

    // ── Internal ─────────────────────────────────────────────

    private static List<String> matchPlayerNames(String partial) {
        String lower = partial.toLowerCase();
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
