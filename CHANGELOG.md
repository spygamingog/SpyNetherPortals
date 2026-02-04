# Changelog

## [1.0.2] - 2026-02-04

### Fixed
- **Hibernation Compatibility**: Fixed a critical issue where portals would fail to function if the target world was in sleep mode. Portals now correctly wake up hibernating worlds upon interaction.
- **Portal Linking**: Resolved inconsistencies where portals would link to incorrect locations or fail to generate a return portal.
- **Coordinate Scaling**: Fixed Nether coordinate scaling (8:1) to ensure accurate player placement when traveling between dimensions.

### Changed
- **Dependency**: Updated to SpyCore 1.0.4 to leverage the new hibernation fixes and improved world management.
