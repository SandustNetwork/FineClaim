# CI/CD setup for FineClaim

Automated builds run on every push/PR to `main`. Publishing runs when a version tag is pushed or when the release workflow is triggered manually.

## Workflows

| Workflow | Trigger | Result |
|---|---|---|
| `CI` | Push/PR to `main` | Builds all configured Folia/Paper versions |
| `Release` | Tag `v*` or manual dispatch | GitHub Release + Modrinth version (via `github-actions[bot]`) |

## Supported Minecraft versions

Build matrix (one JAR per Paper API version):

- `gradle/mc-versions.txt`

Advertised game versions on Modrinth/GitHub (includes `1.21.2`, which shares compatible builds):

- `gradle/modrinth-game-versions.txt`

## Repository configuration

### Secrets (Settings → Secrets and variables → Actions)

| Secret | Required | Purpose |
|---|---|---|
| `MODRINTH_TOKEN` | Yes (for release) | Modrinth API token with **Create version** permission |

Create a token at https://modrinth.com/settings/personal-access-tokens

### Variables (Settings → Secrets and variables → Actions → Variables)

| Variable | Example | Purpose |
|---|---|---|
| `MODRINTH_PROJECT_ID` | `abcd1234` | Modrinth project slug or ID for FineClaim |

Create the Modrinth project first (type: **Plugin**, loaders: **Paper** + **Folia**).

## Releasing

### Recommended: git tag

```bash
git tag v1.0.0
git push origin v1.0.0
```

The `Release` workflow will:

1. Build `FineClaim-<version>-mc<game>.jar` for every version in `gradle/mc-versions.txt`
2. Create a GitHub Release (author: `github-actions[bot]`)
3. Upload all JARs to GitHub Releases
4. Publish the same files to Modrinth

### Manual release (GitHub Actions UI)

1. Actions → **Release** → **Run workflow**
2. Enter version (without `v`, e.g. `1.0.0`)
3. Choose channel: `release`, `beta`, or `alpha`

## Artifact naming

```
FineClaim-1.0.0-mc1.21.4.jar
FineClaim-1.0.0-mc1.21.11.jar
...
```

Pick the JAR whose `mc` suffix matches (or is closest to) your Folia server version.

## Local multi-version build

```bash
./gradlew build -PmcVersion=1.21.4 -PpluginVersion=1.0.0
```

Windows:

```powershell
.\gradlew.bat build -PmcVersion=1.21.4 -PpluginVersion=1.0.0
```

## Troubleshooting

- **Modrinth step skipped or failed**: verify `MODRINTH_TOKEN` and `MODRINTH_PROJECT_ID`.
- **Missing Paper API version**: remove the version from `gradle/mc-versions.txt` if Paper has not published it yet.
- **GitHub Release without Modrinth**: release still succeeds on GitHub if Modrinth credentials are missing; check workflow logs for Modrinth errors.
