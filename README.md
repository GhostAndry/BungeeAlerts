# BungeeAlerts – Cross-Server Anti-Cheat Alert System

## Features

* **Multi-Anti-Cheat Support**: Works with Vulcan, Matrix, GrimAC, and Karhu
* **Redis Integration**: Real-time cross-server alert broadcasting
* **Centralized Logging**: Store violations using MySQL, MongoDB, or SQLite
* **Staff Management**: Toggle alerts via `/bungeealerts`
* **Log Review Panel**: View complete player violation history with `/aclogs`
* **Fully Customizable Messages**: Alerts, hover text, pagination, database settings

---

## Installation

1. Place the plugin inside each backend server’s `plugins/` folder
2. Configure `config.yml`
3. Ensure required services are running:

   * Redis server
   * MySQL / MariaDB / PostgresSQL / MongoDB (optional)
4. Restart all servers in your network

---

## Configuration (`config.yml`)

Below is a simplified preview.
A full version with detailed English comments is included inside the plugin.

```yaml
server_name: "server"

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

redis:
    host: localhost
    port: 6379
    channel: "bungeealerts"

aclogs:

    enabled: true

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

    paginated: "&dAC Logs for %player% (Page %page% of %total_pages%)."
    next-page: "Next page... (> Page %page% of %total_pages%)"
    not-found: "&cNo logs found for %player%."

    storage-method: "mysql"

    sql:
        host: "localhost"
        port: 3306
        database: "aclogs"
        username: "root"
        password: "password"
        use-ssl: false

    mongodb:
        uri: "mongodb://root:password@localhost:27017/admin"
        database: "bungeealerts"
        collection: "aclogs"

    sqlite:
        file: "aclogs.db"
```

---

## Commands

| Command            | Permission         | Description                      |
| ------------------ | ------------------ | -------------------------------- |
| `/bungeealerts`    | `bungeealerts.use` | Toggle anti-cheat alerts         |
| `/balerts`         | `bungeealerts.use` | Command alias                    |
| `/testalert`       | `bungeealerts.test`| Send a test alert to staff       |
| `/aclogs <player>` | `bungeealerts.use` | View stored AC logs for a player |

---

## Requirements

* Java 21+
* Redis 7.2.0+
* MySQL/MariaDB (optional)
* MongoDB (optional)
* PostgreSQL (optional)
* SQLite (local file)
* Any Spigot/Paper server 1.8.X+
* Optional: BungeeCord / Velocity / Forks / network

---

## Supported Anti-Cheats

* Vulcan
* Matrix
* GrimAC
* Karhu
* Polar (planned)


## FOR VULCAN: PLEASE ENABLE `enable-api` IN VULCAN'S CONFIG TO ALLOW ALERTS TO BE SENT PROPERLY.
