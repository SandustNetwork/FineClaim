param(
    [Parameter(Mandatory = $true)]
    [string]$ServerDir
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$libsDir = Join-Path $projectRoot "build\libs"

if (-not (Test-Path $libsDir)) {
    Write-Error "Build output directory not found: $libsDir. Run '.\gradlew.bat build' first."
}

$jars = Get-ChildItem -Path $libsDir -Filter "*.jar" |
    Where-Object { $_.Name -notmatch "-sources|-javadoc" } |
    Sort-Object LastWriteTime -Descending

if ($jars.Count -eq 0) {
    Write-Error "No plugin JAR found in $libsDir. Run '.\gradlew.bat build' first."
}

$sourceJar = $jars[0]
$pluginsDir = Join-Path $ServerDir "plugins"

if (-not (Test-Path $pluginsDir)) {
    New-Item -ItemType Directory -Path $pluginsDir -Force | Out-Null
}

$destination = Join-Path $pluginsDir $sourceJar.Name
Copy-Item -Path $sourceJar.FullName -Destination $destination -Force

Write-Host "Copied $($sourceJar.Name) to $destination"
