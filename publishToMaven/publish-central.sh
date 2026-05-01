#!/usr/bin/env bash
# Publishes ToastX library modules to Maven Central. Disables configuration cache (required by vanniktech).
#
# vanniktech resolves mavenCentralUsername/password as normal Gradle project properties (same as root
# gradle.properties). If they only exist in a submodule file, Sonatype can fail (#866). Exporting
# ORG_GRADLE_PROJECT_* injects them for the root project before Gradle starts.
set -euo pipefail
cd "$(dirname "$0")/.."

# Fixes "gpg: signing failed: Inappropriate ioctl for device" when pinentry expects a TTY.
if [[ -t 0 ]] && command -v tty >/dev/null 2>&1; then
  export GPG_TTY="$(tty)"
fi
SECRETS="$(pwd)/publishToMaven/secrets.properties"
if [[ -f "$SECRETS" ]]; then
  _u="$(grep -E '^mavenCentralUsername=' "$SECRETS" | head -1 | cut -d= -f2- || true)"
  _p="$(grep -E '^mavenCentralPassword=' "$SECRETS" | head -1 | cut -d= -f2- || true)"
  if [[ -n "${_u:-}" ]]; then export ORG_GRADLE_PROJECT_mavenCentralUsername="$_u"; fi
  if [[ -n "${_p:-}" ]]; then export ORG_GRADLE_PROJECT_mavenCentralPassword="$_p"; fi
fi
exec ./gradlew \
  :toastxLib:publishAndReleaseToMavenCentral \
  --no-configuration-cache \
  -PSONATYPE_HOST=CENTRAL_PORTAL \
  -PSONATYPE_AUTOMATIC_RELEASE=true \
  "$@"
