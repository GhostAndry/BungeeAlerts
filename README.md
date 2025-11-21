
# BungeeAlerts - Cross-Server Anti-Cheat Alert System

## Features
- **Multi-AC Compatibility**: Works with Vulcan, Matrix, GrimAC, and Karhu
- **Redis Integration**: Real-time alerts across your BungeeCord network
- **Centralized Logging**: Stores alerts in MySQL database
- **Staff Management**: Toggle alerts with `/bungeealerts` command
- **Log Review**: View player histories with `/aclogs` command
- **Customizable Messages**: Fully configurable alert formats

## Installation
1. Place the plugin in all backend servers' `plugins/` folders
2. Configure `config.yml` (see Configuration section)
3. Install required dependencies:
   - Redis server
   - MySQL database
4. Restart your servers

## Configuration (`config.yml`)
```yaml
#
#     ██████╗ ██╗   ██╗███╗   ██╗ ██████╗ ███████╗███████╗
#     ██╔══██╗██║   ██║████╗  ██║██╔════╝ ██╔════╝██╔════╝
#     ██████╔╝██║   ██║██╔██╗ ██║██║  ███╗█████╗  █████╗
#     ██╔══██╗██║   ██║██║╚██╗██║██║   ██║██╔══╝  ██╔══╝
#     ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝███████╗███████╗
#     ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝╚══════╝
#
#       █████╗ ██╗     ███████╗██████╗ ████████╗███████╗
#      ██╔══██╗██║     ██╔════╝██╔══██╗╚══██╔══╝██╔════╝
#      ███████║██║     █████╗  ██████╔╝   ██║   ███████╗
#      ██╔══██║██║     ██╔══╝  ██╔══██╗   ██║   ╚════██║
#      ██║  ██║███████╗███████╗██║  ██║   ██║   ███████║
#      ╚═╝  ╚═╝╚══════╝╚══════╝╚═╝  ╚═╝   ╚═╝   ╚══════╝
#
#  By GhostAndry Compatible with Vulcan, Matrix, GrimAC and Karhu
#  Version: 1.0.0
#

# Server identifier shown in alerts and logs
# Used in DB.add() and flag event handlers
server_name: "server"

# Alert message template broadcasted when cheat is detected
# Format variables:
#   %server% - Server name from config
#   %player% - Player who triggered the alert
#   %check%  - Anti-cheat check name
#   %vl%     - Violation level
# Used in on[Matrix|Vulcan|Grim]FlagEvent handlers
alert_message: "&8[&5&lAntiCheat&8] &7(%server%) &d%player% &7> &d%check% &8(&d%vl%&8)"
hover_message: |
    &6Player: &e%player%
    &6Check: &e%check%
    &6Description: &7%description%
    &6Violations: &e%vl%
    &6Server: &e%server%
    &6Time: &e%time%
    &6Details:
    &7%check_info%

# Redis configuration for cross-server messaging
# Used in RedisManager class
redis:
    # Redis server hostname/IP
    host: localhost
    # Redis server port
    port: 6379
    # Pub/Sub channel name for alerts
    channel: "bungeealerts"

# Anti-Cheat logging system configuration
# Used throughout DB.java and CommandACLogs
aclogs:
    # Master switch for logging system
    # Checked in DB.add() before logging
    enabled: true
    # Format for individual log entries
    # Format variables:
    #   %time%    - Timestamp of violation
    #   %player%  - Player name
    #   %check%   - Check name
    #   %vl%      - Violation level
    #   %server%  - Server name
    # Used in DB.formatMessage()
    message: "&7(%time%) &d%player% &7> &d%check% &8(&d%vl%&8) &8[&d%server%&8]"
    hover_message: |
        &6Player: &e%player%
        &6Check: &e%check%
        &6Description: &7%description%
        &6Violations: &e%vl%
        &6Server: &e%server%
        &6Time: &e%time%
        &6Details:
        &7%check_info%
    # Header for paginated log results
    # Used in CommandACLogs
    paginated: "&dAC Logs for %player% (Page %page% of %total_pages%)."
    # Message shown when more pages are available
    # Used in CommandACLogs pagination
    next-page: "Next page... (> Page %page% of %total_pages%)"
    # Error message when no logs found for player
    # Used in CommandACLogs
    not-found: "&cNo logs found for %player%."

    mysql:
        host: "localhost"
        port: 3306
        database: "aclogs"
        username: "root"
        password: "password"
```

## Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/bungeealerts` | `bungeealerts.use` | Toggle AC alerts |
| `/balerts` | `bungeealerts.use` | Alias for above |
| `/aclogs <player>` | `bungeealerts.use` | View player's AC history |

## Requirements
- Java 21+
- Redis 7.2.0+
- MySQL 10.5+
- BungeeCord/Waterfall network (optional)
- Spigot/Paper 1.8.X+ servers

## Support
Compatible with:
- ✅ Vulcan
- ✅ Matrix
- ✅ GrimAC
- ✅ Karhu
- ❗ Polar (TODO)