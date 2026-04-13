#!/usr/bin/env bash
# Runs Gradle from the project root (directory above this script).
# Usage:
#   ./publishToMaven/publish.sh
#   ./publishToMaven/publish.sh publishToMavenCentral
#   ./publishToMaven/publish.sh publishAllPublicationsToMavenFolderRepoRepository

set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT"

TASK="${1:-publishToMavenLocal}"
shift || true

echo "($ROOT) ./gradlew $TASK $*"
exec ./gradlew "$TASK" "$@"
