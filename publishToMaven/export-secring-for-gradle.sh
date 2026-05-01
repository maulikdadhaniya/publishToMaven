#!/usr/bin/env bash
# Run this in your own terminal (not headless) so gpg-agent can unlock the key.
# Usage: ./publishToMaven/export-secring-for-gradle.sh KEY_ID
# KEY_ID: gpg --list-secret-keys --keyid-format LONG  (sec line)
set -euo pipefail
KEY_ID="${1:-}"
if [[ -z "$KEY_ID" ]]; then
  echo "Usage: $0 KEY_ID" >&2
  echo "List keys: gpg --list-secret-keys --keyid-format LONG" >&2
  exit 1
fi
OUT="${HOME}/.gnupg/gradle-secring.gpg"
mkdir -p "$(dirname "$OUT")"
gpg --output "$OUT" --export-secret-keys "$KEY_ID"
echo "Wrote $OUT ($(wc -c < "$OUT" | tr -d ' ') bytes)"
echo "Ensure publishToMaven/secrets.properties has:"
echo "  signing.secretKeyRingFile=$OUT"
echo "  signing.keyId=<8 hex chars, e.g. A4D45791 for key 2F3794B7A4D45791>"
