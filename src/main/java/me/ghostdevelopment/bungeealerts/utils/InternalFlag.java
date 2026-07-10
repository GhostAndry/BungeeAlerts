package me.ghostdevelopment.bungeealerts.utils;

/**
 * Standardized pre-dispatch data from any anti-cheat event.
 * <p>
 * Every AC listener (Vulcan, Matrix, Grim, Karhu) maps its
 * vendor-specific event to this common record. {@link me.ghostdevelopment.bungeealerts.AlertDispatcher}
 * then attaches timestamp and server to produce a full {@link Check}.
 * <p>
 * This eliminates the 5-parameter dispatch method and makes
 * adding new AC support a single mapping function.
 *
 * @param playerName  the flagged player
 * @param checkName   anti-cheat check name (e.g. "SPEED_A")
 * @param violations  violation level
 * @param description human-readable summary
 * @param checkInfo   detailed debug information for hover text
 */
public record InternalFlag(
        String playerName,
        String checkName,
        int violations,
        String description,
        String checkInfo
) {}
