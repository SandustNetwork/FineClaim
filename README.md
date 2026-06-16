# FineClaim

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

A lightweight chunk-based land claim plugin for **Folia** and Paper (Minecraft 1.21.x).

Repository: https://github.com/SandustNetwork/FineClaim

## Features

- Chunk-based claims (`/claim`, `/unclaim`)
- Trust management (`/trust`, `/untrust`)
- Claim info (`/claiminfo`)
- Block break/place/interact protection
- YAML file persistence (`plugins/FineClaim/claims.yml`)
- Bukkit permission nodes and admin bypass

## Requirements

- Java 21
- Folia or Paper 1.21.x

## Build

Windows:

```powershell
.\gradlew.bat build
```

Linux/macOS:

```bash
./gradlew build
```

The plugin JAR is written to `build/libs/`.

## Development test

Manual test checklist: [docs/TESTING.md](docs/TESTING.md)

Copy the latest built JAR to a local test server:

Windows:

```powershell
.\scripts\copy-plugin.ps1 -ServerDir "D:\FoliaTest"
```

Linux/macOS:

```bash
chmod +x ./scripts/copy-plugin.sh
./scripts/copy-plugin.sh ./FoliaTest
```

Replace the server path with your Folia test server directory.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

Copyright 2026 Sandust Network

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full license text.
