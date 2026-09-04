# SpyNetherPortals

[![Platform](https://img.shields.io/badge/Platform-Paper%20%2F%20Purpur-blue)](https://papermc.io)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21%2B-green)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://www.oracle.com/java/)
[![Core](https://img.shields.io/badge/Requires-SpyCore%201.1.0-purple)](https://github.com/spygamingog/SpyCore)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

SpyNetherPortals is a dimension-linking and portal routing plugin for Paper and Purpur (1.21+). Designed as an extension for [SpyCore](https://github.com/spygamingog/SpyCore), it links Nether and End portals between custom worlds and containerized worlds, handles coordinate translation, and enables cross-dimension communication.

---

## Features

- **Automatic Portal Routing**: Links Overworld, Nether, and End portals across custom worlds using standard naming conventions (`<world>`, `<world>_nether`, `<world>_the_end`) and container folders.
- **8:1 Coordinate Scaling**: Correctly translates coordinates between the Overworld and Nether according to vanilla ratios (8:1 horizontal scale), ensuring return portals link up properly.
- **On-Demand World Wakeup**: Works with SpyCore's hibernation system. If a destination Nether or End world is sleeping or unloaded, entering a portal safely wakes it up before teleporting the player.
- **Linked Dimension Chat**: While SpyCore isolates chat and tablists between separate worlds by default, SpyNetherPortals bridges players across linked dimensions so players in `survival` and `survival_nether` can talk to each other and see each other on tab.
- **Arena & Match Isolation**: Prevents unauthorized portal creation and dimension hopping in temporary minigame instances.

---

## Requirements & Installation

1. **Requirements**:
   - Paper or Purpur 1.21+
   - Java 21+
   - [SpyCore](https://github.com/spygamingog/SpyCore) 1.1.0 or newer
2. **Installation**:
   - Put both `spycore-1.1.0.jar` and `spynetherportals-1.0.5.jar` into your server's `plugins/` directory.
   - Restart the server.

---

## How It Links Worlds

By default, SpyNetherPortals detects corresponding dimensions using suffixes:
- `<world>` &rarr; Overworld
- `<world>_nether` &rarr; Nether
- `<world>_the_end` &rarr; The End

If a player steps into a Nether portal in `world2`, the plugin checks for `world2_nether`. If the target world is hibernating, SpyCore wakes it, calculates the scaled coordinates, and generates or connects to an existing Nether portal.

---

## Sibling Plugins

- **[SpyCore](https://github.com/spygamingog/SpyCore)**: Multi-world management engine and VFS container system.
- **[SpyInventories](https://github.com/spygamingog/SpyInventories)**: Multi-world inventory and player state separation.

---

## License

This project is licensed under the [MIT License](LICENSE).
