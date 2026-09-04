# Changelog

## [1.0.5] - 2026-09-04

### Changed
- **Core Dependency**: Updated to **SpyCore 1.1.0** to support enhanced VFS containers, thread safety, and modern Paper 1.21+ API.
- **Portal Teleportation Safety**: Enhanced portal teleportation safety checks for temporary match worlds and hibernating containers.
- **Build Modernization**: Updated to clean compiler configuration compatible with Java 21+.

## [1.0.4] - 2026-02-12

### Changed
- **Group Isolation Logic**: Took ownership of Chat and Tablist grouping logic. While SpyCore enforces strict single-world isolation, SpyNetherPortals now explicitly bridges this for linked worlds (Nether/End) to allow group communication and visibility.
- **Dependency**: Updated to SpyCore 1.0.7 to ensure compatibility with the latest VFS and world management improvements.
- Internal optimizations for portal connection handling.


## [1.0.3] - 2026-02-05

### Added
- **Isolated Messaging**: Join, Quit, and Death messages are now isolated to "World Groups" (Overworld + Nether + End set). Players in one group will not see these messages from another group.
- **Advancement Isolation**: Advancement announcements are now processed per world group (requires GameRule logic handling if enabled).

## [1.0.2] - 2026-02-04

### Fixed
- **Hibernation Compatibility**: Fixed a critical issue where portals would fail to function if the target world was in sleep mode. Portals now correctly wake up hibernating worlds upon interaction.
- **Portal Linking**: Resolved inconsistencies where portals would link to incorrect locations or fail to generate a return portal.
- **Coordinate Scaling**: Fixed Nether coordinate scaling (8:1) to ensure accurate player placement when traveling between dimensions.

### Changed
- **Dependency**: Updated to SpyCore 1.0.4 to leverage the new hibernation fixes and improved world management.
