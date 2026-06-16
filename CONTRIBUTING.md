# Contributing to FineClaim

Thank you for your interest in contributing to FineClaim.

## Before you start

- Search [existing issues](https://github.com/SandustNetwork/FineClaim/issues) to avoid duplicates.
- For bugs, use the **Bug Report** issue template.
- For features, use the **Feature Request** issue template.

## Development setup

Requirements:

- Java 21
- Gradle Wrapper (included in the repository)

Build:

```powershell
# Windows
.\gradlew.bat build
```

```bash
# Linux/macOS
./gradlew build
```

Copy the built plugin to a local Folia test server:

```powershell
.\scripts\copy-plugin.ps1 -ServerDir "path\to\your\server"
```

```bash
./scripts/copy-plugin.sh ./path/to/your/server
```

Manual testing checklist: [docs/TESTING.md](docs/TESTING.md)

## Code guidelines

- Package root: `com.sandustnetwork.fineclaim`
- Java 21, 4-space indentation (see [.editorconfig](.editorconfig))
- No wildcard imports, no Lombok, no NMS/CraftBukkit/reflection
- Keep domain logic free of Bukkit/Paper API dependencies
- Business errors should use result objects instead of throwing where appropriate
- Target Folia-first compatibility (avoid legacy `BukkitScheduler` unless explicitly required)

## Pull requests

1. Fork the repository and create a feature branch.
2. Keep changes focused and easy to review.
3. Ensure the build passes locally.
4. Fill in the pull request template completely.
5. Link related issues when applicable.

## License

By contributing, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).
