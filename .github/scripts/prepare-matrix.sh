#!/usr/bin/env bash
set -euo pipefail

read_list() {
  local file="$1"
  grep -v '^\s*#' "$file" | grep -v '^\s*$' | jq -R -s -c 'split("\n") | map(select(length > 0))'
}

mc_versions="$(read_list gradle/mc-versions.txt)"
modrinth_game_versions="$(read_list gradle/modrinth-game-versions.txt)"

{
  echo "mc-versions=$mc_versions"
  echo "modrinth-game-versions=$modrinth_game_versions"
} >> "$GITHUB_OUTPUT"
