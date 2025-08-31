ConfigurableHeadDrop
=====================

A small Paper plugin for Minecraft 1.21.8 that adds configurable player and mob head drops.

Features
- Configure whether player heads drop only when killed by another player.
- Optionally give the head directly to the killer's inventory (fallback: drop on ground).
- Configure percentage chances for player heads and mob heads, including per-mob entries.

Build

Make sure you have Java 17+ and Maven installed.

To build the plugin:

```pwsh
mvn clean package
```

Drop the resulting JAR from `target/` into your Paper server's `plugins/` folder.

Configuration

Edit `config.yml` inside the plugin folder after first run. Options:
- `player-heads.only-when-killed-by-player` (boolean)
- `player-heads.give-to-killer` (boolean)
- `player-heads.default-chance-percent` (double)
- `mob-heads.default-chance-percent` (double)
- `mob-heads.chances` map for per-entity percentages (use entity type names, e.g. `CREEPER`)

Permissions
- `configurableheaddrop.reload` (default: op) — allows reloading the plugin config using `/headdrop reload`.

Publishing
- Clean up package coordinates in `pom.xml` (groupId/artifactId/version) to match your repository naming.
- Add a tag and push the repo to GitHub. Create a GitHub release and attach `target/configurable-headdrop-1.0-SNAPSHOT.jar` or rename version to a stable tag before packaging.

Notes
- This project uses the Paper API. Make sure your server version matches the Paper API version configured in `pom.xml`.
