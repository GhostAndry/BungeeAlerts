package me.ghostdevelopment.bungeealerts;

import me.ghostdevelopment.bungeealerts.redis.RedisManager;
import me.ghostdevelopment.bungeealerts.utils.Check;
import me.ghostdevelopment.bungeealerts.utils.DB;
import me.ghostdevelopment.bungeealerts.utils.InternalFlag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Single entry point for dispatching anti-cheat alerts.
 * <p>
 * Every event handler (Vulcan, Matrix, Grim, Karhu) used to duplicate
 * the same 5 steps: timestamp, Check creation, console log, Redis send,
 * DB insert. Now they call one method here.
 * <p>
 * Config template strings are resolved once at construction time and
 * reused — no repeated {@code getConfig().getString()} on every alert.
 */
public final class AlertDispatcher {

    private final BungeeAlerts plugin;
    private final Logger logger;

    /** Cached config templates — resolved once, used many times. */
    private final String alertFormat;
    private final String hoverFormat;

    public AlertDispatcher(BungeeAlerts plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.alertFormat = plugin.getConfig().getString(Settings.CFG_ALERT_MESSAGE, "");
        this.hoverFormat = plugin.getConfig().getString(Settings.CFG_HOVER_MESSAGE, "");
    }

    /**
     * Full pipeline from a vendor-neutral {@link InternalFlag}.
     * <p>
     * This is the preferred entry point — every AC listener maps its
     * vendor-specific event to an InternalFlag and calls this method.
     *
     * @param flag the standardized flag data
     */
    public void dispatch(InternalFlag flag) {
        dispatch(flag.playerName(), flag.checkName(), flag.violations(),
                flag.description(), flag.checkInfo());
    }

    /**
     * Full pipeline: build timestamp, create Check, log to console,
     * broadcast via Redis, persist to database.
     *
     * @param playerName  the flagged player
     * @param checkName   the anti-cheat check name
     * @param violations  violation level
     * @param description human-readable description
     * @param checkInfo   detailed debug information
     */
    public void dispatch(String playerName, String checkName, int violations,
                         String description, String checkInfo) {

        String serverName = plugin.getConfig().getString(
                Settings.CFG_SERVER_NAME, Settings.DEFAULT_SERVER_NAME);

        Check check = new Check(now(), playerName, checkName,
                violations, serverName, description, checkInfo);

        // 1. Console log (clean, no color codes)
        logger.info(Settings.stripColors(Settings.format(alertFormat, check)));

        // 2. Redis broadcast (handles staff messaging internally)
        RedisManager redis = BungeeAlerts.getRedisManager();
        if (redis != null) {
            redis.sendCheck(check);
        }

        // 3. Database persistence
        DB.add(check);
    }

    /**
     * Sends a formatted hoverable alert directly to all online staff.
     * Used by Redis listener when receiving alerts from other servers.
     */
    public void sendToStaff(Check check) {
        Collection<Player> staff = BungeeAlerts.getStaffer();
        if (staff.isEmpty()) return;

        String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                Settings.format(alertFormat, check));
        String hoverText = ChatColor.translateAlternateColorCodes('&',
                Settings.format(hoverFormat, check));

        TextComponent component = new TextComponent(formattedMessage);
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hoverText).create()));

        for (Player staffer : staff) {
            if (staffer.isOnline()) {
                staffer.spigot().sendMessage(component);
            }
        }
    }

    /**
     * Sends a plain-text message to all online staff (no hover).
     */
    public void sendPlainToStaff(String message) {
        Collection<Player> staff = BungeeAlerts.getStaffer();
        if (staff.isEmpty()) return;

        String formatted = ChatColor.translateAlternateColorCodes('&', message);
        for (Player staffer : staff) {
            if (staffer.isOnline()) {
                staffer.sendMessage(formatted);
            }
        }
    }

    // ── internal ─────────────────────────────────────────────

    /** Microsecond-precision timestamp, synchronized across servers via Redis. */
    public static String now() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());
    }
}
