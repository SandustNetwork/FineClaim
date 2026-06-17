# FineClaim

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A lightweight **3D box land claim** plugin for **Folia** and Paper (Minecraft 1.21.x).

Players select a precise region with a claim tool, preview the border with particles, then confirm. Claims protect block break, place, and interact — without relying on whole-chunk ownership.

Repository: https://github.com/SandustNetwork/FineClaim

## Highlights

- **3D box claims** — select corner A and corner B; limits are counted in **blocks**, not chunks
- **Claim tool workflow** — `/claim` gives a brick selection item; left-click = A, right-click = B
- **Particle border preview** — multi-color outline before confirm; no virtual blocks left behind
- **Multiple regions per player** — each confirmed selection creates an independent claim box
- **Resize** — `/claim resize` to re-select A/B inside an owned claim
- **Trust system** — per-claim trust for build access
- **Folia-safe** — region-aware schedulers; no global `BukkitScheduler`
- **YAML persistence** — `plugins/FineClaim/claims.yml` with automatic migration from older formats
- **Tab completion** — `/claim` subcommand suggestions via Paper `BasicCommand`

## Requirements

- Java 21
- Folia or Paper **1.21.x**

## Installation

1. Build the plugin (see [Build](#build)) or download a release JAR.
2. Copy `FineClaim-*.jar` into your server's `plugins/` folder.
3. Start the server once to generate `plugins/FineClaim/config.yml`.
4. Restart or run `/claim reload` after editing config.

## Commands

| Command | Description |
|---|---|
| `/claim` | Start a new claim selection (equips the claim tool) |
| `/claim confirm` | Create the claim from the current A/B preview |
| `/claim cancel` | Cancel selection, remove tool, stop preview |
| `/claim resize` | Start resize mode in an owned claim (re-select A/B) |
| `/claim resize confirm` | Apply the resized box |
| `/claim resize cancel` | Cancel resize |
| `/claim info` | Show claim panel and border preview |
| `/claim unclaim` | Delete the claim box at your location |
| `/claim trust <player>` | Trust a player in the claim at your location |
| `/claim untrust <player>` | Remove trust |
| `/claim reload` | Reload `config.yml` and `claims.yml` (admin) |

Press **Tab** after `/claim` for subcommand suggestions.

## Claim tool

1. Run `/claim`.
2. You receive a **Claim Tool** (brick item with enchant glow).
3. **Left-click** a block → point **A**.
4. **Right-click** a block → point **B**.
5. A colored particle frame appears between the corners.
6. Run `/claim confirm` to save, or `/claim cancel` to abort.

The tool is removed automatically on confirm, cancel, quit, or preview expiry.

## Permissions

| Node | Default | Description |
|---|---|---|
| `fineclaim.command.claim` | `true` | Use `/claim` (selection, confirm, cancel, resize) |
| `fineclaim.command.unclaim` | `true` | Use `/claim unclaim` |
| `fineclaim.command.trust` | `true` | Use `/claim trust` |
| `fineclaim.command.untrust` | `true` | Use `/claim untrust` |
| `fineclaim.command.info` | `true` | Use `/claim info` |
| `fineclaim.admin.bypass` | `op` | Bypass claim protection |
| `fineclaim.admin.info` | `op` | View any claim with `/claim info` |
| `fineclaim.admin.reload` | `op` | Use `/claim reload` |

## Configuration

Default file: `plugins/FineClaim/config.yml`

```yaml
MaxBlocksPerMember: 4096
MaxBlocksPerServer: 2500000
PreviewDisplaySeconds: 120
CornerParticle: END_ROD
BorderColors:
  - "#00B4FC"
  - "#6EE7FF"
  - "#A78BFA"
  - "#F472B6"
  - "#34D399"
ParticleSpacing: 0.75
MaxParticlesPerEdge: 64
BorderRefreshTicks: 10
MinClaimHeight: -64
MaxClaimHeight: 320
```

| Key | Description |
|---|---|
| `MaxBlocksPerMember` | Total blocks a player may claim across all regions |
| `MaxBlocksPerServer` | Total claimed blocks on the server |
| `PreviewDisplaySeconds` | How long selection/border preview stays visible |
| `CornerParticle` | Accent particle at box corners (e.g. `END_ROD`) |
| `BorderColors` | Hex colors for gradient edge particles |
| `ParticleSpacing` | Distance between particles along each edge |
| `MaxParticlesPerEdge` | Cap per edge (performance guard for large boxes) |
| `BorderRefreshTicks` | How often the particle frame is redrawn |
| `MinClaimHeight` / `MaxClaimHeight` | Used when migrating legacy chunk data to boxes |

Legacy keys such as `MaxChunksPerMember` or `BorderBlock` are still read with sensible fallbacks.

## Data storage

Claims are stored in `plugins/FineClaim/claims.yml`:

```yaml
claims:
  - id: ...
    owner: ...
    createdAt: ...
    trustedPlayers: []
    box:
      worldName: world
      minX: 10
      minY: 64
      minZ: -5
      maxX: 25
      maxY: 80
      maxZ: 10
```

Older chunk-based entries are migrated automatically on load.

## Build

Windows:

```powershell
.\gradlew.bat build
```

Linux/macOS:

```bash
./gradlew build
```

Output JAR: `build/libs/FineClaim-*.jar`

## Local testing

Manual checklist: [docs/TESTING.md](docs/TESTING.md)

Copy the built JAR to a test server:

Windows:

```powershell
.\scripts\copy-plugin.ps1 -ServerDir "D:\FoliaTest"
```

Linux/macOS:

```bash
chmod +x ./scripts/copy-plugin.sh
./scripts/copy-plugin.sh ./FoliaTest
```

Replace the path with your Folia/Paper server directory.

## Project layout

```
src/main/java/com/sandustnetwork/fineclaim/
├── FineClaimPlugin.java          # Bootstrap & command registration
├── claim/
│   ├── application/              # ClaimService, business rules
│   ├── command/                  # Player & admin commands
│   ├── config/                   # Settings & limit checks
│   ├── domain/                   # Claim, ClaimBox, BlockPos
│   ├── protection/               # Break/place/interact listener
│   ├── storage/                  # Repository & YAML codec
│   ├── visual/                   # Particle border preview
│   └── wand/                     # Claim tool & click handling
└── permission/                   # Permission helpers
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

Copyright 2026 Sandust Network

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full license text.
