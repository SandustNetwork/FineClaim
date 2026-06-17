#!/usr/bin/env bash
set -euo pipefail

resolve_version() {
  if [[ -n "${INPUT_VERSION:-}" ]]; then
    echo "$INPUT_VERSION"
    return
  fi

  local ref_name="${GITHUB_REF_NAME:-}"
  if [[ "$ref_name" =~ ^v(.+)$ ]]; then
    echo "${BASH_REMATCH[1]}"
    return
  fi

  echo "Unable to resolve release version from tag or workflow input." >&2
  exit 1
}

resolve_version_type() {
  if [[ -n "${INPUT_VERSION_TYPE:-}" ]]; then
    echo "$INPUT_VERSION_TYPE"
    return
  fi

  local version="$1"
  local lower
  lower="$(echo "$version" | tr '[:upper:]' '[:lower:]')"

  if [[ "$lower" == *"-alpha"* || "$lower" == *"-a."* ]]; then
    echo "alpha"
  elif [[ "$lower" == *"-beta"* || "$lower" == *"-rc"* || "$lower" == *"-pre"* ]]; then
    echo "beta"
  else
    echo "release"
  fi
}

VERSION="$(resolve_version)"
VERSION_TYPE="$(resolve_version_type "$VERSION")"
TAG="v${VERSION}"

{
  echo "version=$VERSION"
  echo "version-type=$VERSION_TYPE"
  echo "tag=$TAG"
  echo "release-name=FineClaim $VERSION"
} >> "$GITHUB_OUTPUT"
