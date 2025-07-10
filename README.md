
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

```

## Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/bungeealerts` | `bungeealerts.use` | Toggle AC alerts |
| `/balerts` | `bungeealerts.use` | Alias for above |
| `/aclogs <player>` | `bungeealerts.use` | View player's AC history |

## Requirements
- Java 17+
- Redis 5.0+
- MySQL 5.7+
- BungeeCord/Waterfall network
- Spigot/Paper 1.21+ servers

## Support
Compatible with:
- ✅ Vulcan
- ✅ Matrix
- ✅ GrimAC
- ✅ Karhu
- ❗ Polar (TODO)