# WhispWaypoints Versioning Guide

## Version Format
**Format**: `MinecraftVersion_PluginVersion`
- Example: `1.21.8_1.6.0`

## Version Components

### Minecraft Version (1.21.8)
- Should match the target Minecraft version
- Update when upgrading compatibility to new Minecraft releases
- Currently targeting: **1.21.8**

### Plugin Version (1.6.0)
**Semantic Versioning**: `MAJOR.MINOR.PATCH`

#### MAJOR Version (1.x.x)
Increment when making **incompatible API changes** or **breaking changes**:
- Breaking configuration format changes
- Incompatible data file format changes  
- Removal of major features
- Complete system redesigns

#### MINOR Version (x.6.x)
Increment when adding **new features** in a backwards-compatible manner:
- New commands or functionality
- New configuration options
- New game mechanics (camp system, banner system)
- Significant UX improvements
- New resource pack features

#### PATCH Version (x.x.0)
Increment for **backwards-compatible bug fixes**:
- Bug fixes that don't change functionality
- Performance improvements
- Code cleanup without behavior changes
- Documentation updates

## Version Update Process

### Manual Version Updates
1. **Edit `version.properties`**:
   ```properties
   minecraft=1.21.8
   plugin_major=1
   plugin_minor=6  
   plugin_patch=0
   ```

2. **Commit with descriptive message**:
   ```bash
   git commit -m "Bump version to 1.21.8_1.6.0 - [reason for version bump]"
   ```

### Automated Version Updates
Use PowerShell build scripts for automatic incrementing:

```powershell
# Increment patch version (1.5.0 → 1.5.1)
.\build.ps1 -IncrementPatch

# Increment minor version (1.5.0 → 1.6.0) 
.\build.ps1 -IncrementMinor

# Increment major version (1.5.0 → 2.0.0)
.\build.ps1 -IncrementMajor
```

## Version History Examples

### Recent Version Changes
- **1.21.8_1.5.0**: Banner persistence system, non-throwable tokens
- **1.21.8_1.6.0**: Direct token consumption system (removed addtoken command)

### When to Increment Which Version

#### PATCH Examples (x.x.+1):
- Fixed banner colors not saving correctly
- Improved performance of waypoint loading
- Fixed typo in help messages

#### MINOR Examples (x.+1.0):
- Added camp banner protection system
- Implemented custom token appearance
- Added reload commands
- Introduced direct token consumption

#### MAJOR Examples (+1.0.0):
- Complete rewrite from ender pearl tokens to paper tokens
- Breaking change to data file format
- Removal of old waypoint system
- API breaking changes for other plugins

## Release Checklist
- [ ] Update version in `version.properties`
- [ ] Test build: `.\gradlew clean build`
- [ ] Update changelog/commit message with version bump reason
- [ ] Commit changes
- [ ] Tag release (optional): `git tag v1.21.8_1.6.0`
- [ ] Build final JAR: `.\build.ps1 -BuildPack` (if resource pack needed)

## Current Version
**Latest**: `1.21.8_1.6.0`
**Minecraft Target**: Paper 1.21.1+
**Java**: 21+
