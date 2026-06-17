#!/usr/bin/env bash
set -euo pipefail

output_file="${1:-release-notes.md}"
previous_tag="$(git describe --tags --abbrev=0 HEAD^ 2>/dev/null || true)"

{
  echo "## FineClaim ${VERSION:-unknown}"
  echo
  if [[ -n "$previous_tag" ]]; then
    echo "Changes since \`$previous_tag\`:"
    echo
    git log --pretty=format:'- %s (%h)' "${previous_tag}..HEAD"
  else
    echo "Initial release."
    echo
    git log --pretty=format:'- %s (%h)' -n 20
  fi
  echo
} > "$output_file"
