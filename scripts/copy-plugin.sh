#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <ServerDir>" >&2
    echo "Example: $0 ./FoliaTest" >&2
    exit 1
fi

SERVER_DIR="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LIBS_DIR="$PROJECT_ROOT/build/libs"

if [ ! -d "$LIBS_DIR" ]; then
    echo "Error: Build output directory not found: $LIBS_DIR. Run './gradlew build' first." >&2
    exit 1
fi

JAR=""
for candidate in "$LIBS_DIR"/*.jar; do
    [ -f "$candidate" ] || continue
    case "$candidate" in
        *-sources.jar|*-javadoc.jar)
            continue
            ;;
    esac

    if [ -z "$JAR" ] || [ "$candidate" -nt "$JAR" ]; then
        JAR="$candidate"
    fi
done

if [ -z "$JAR" ]; then
    echo "Error: No plugin JAR found in $LIBS_DIR. Run './gradlew build' first." >&2
    exit 1
fi

PLUGINS_DIR="$SERVER_DIR/plugins"
mkdir -p "$PLUGINS_DIR"

DESTINATION="$PLUGINS_DIR/$(basename "$JAR")"
cp "$JAR" "$DESTINATION"

echo "Copied $(basename "$JAR") to $DESTINATION"
