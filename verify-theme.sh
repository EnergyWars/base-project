#!/bin/bash

set -e

APP_NAME="base"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UI_DIR="app/src/main/java/com/wafflehq/${APP_NAME}/ui"
SETTINGS_LIB_DIR="lib/settings/src/main/kotlin/com/wafflehq/lib/settings"
PALETTE_FILE="${SETTINGS_LIB_DIR}/colors/ColorPalette.kt"
RAMP_TABLE_FILE="${SETTINGS_LIB_DIR}/colors/ColorRampTable.kt"
THEME_FILE="${UI_DIR}/theme/Theme.kt"
HASH_FILE="theme-hashes.sha256"

cd "$PROJECT_DIR"

echo "=== Theme Verification Script ==="
echo ""

SCAN_DIRS=("$UI_DIR" "$SETTINGS_LIB_DIR")

echo "Check 1: Verifying raw token isolation..."
VIOLATIONS=$(grep -rn \
  --include="*.kt" \
  --exclude-dir=".git" \
  --exclude-dir="build" \
  -E "\b((Sapphire|Aquamarine|Amethyst|Emerald|Citrine|Garnet|Graphite)(10|20|30|40|50|60|70|80|90)|(Dark|Light)(Background|Surface|Surface2|Surface3|Surface4)|Ink[23]?(Dark|Light)|Hairline(Strong)?(Dark|Light))\b" \
  "${SCAN_DIRS[@]}" \
  | grep -v "$PALETTE_FILE" \
  | grep -v "$RAMP_TABLE_FILE" \
  | grep -v "$THEME_FILE" \
  || true)

if [ -n "$VIOLATIONS" ]; then
  echo "❌ FAILED: Raw theme tokens found outside ColorPalette.kt/Theme.kt:"
  echo "$VIOLATIONS"
  exit 1
fi
echo "✓ Passed: Raw tokens only in ColorPalette.kt/Theme.kt"
echo ""

echo "Check 2: Verifying no hardcoded colors..."
HEX_VIOLATIONS=$(grep -rn \
  --include="*.kt" \
  --exclude-dir=".git" \
  --exclude-dir="build" \
  -E "Color\(0x[A-Fa-f0-9]{6,8}\)|\bColor\.(Red|Green|Blue|Cyan|Magenta|Yellow|Gray|LightGray|DarkGray)\b" \
  "${SCAN_DIRS[@]}" \
  | grep -v "$PALETTE_FILE" \
  | grep -v "$THEME_FILE" \
  | grep -v "/theme/Type.kt" \
  || true)

if [ -n "$HEX_VIOLATIONS" ]; then
  echo "❌ FAILED: Hardcoded colors found outside theme:"
  echo "$HEX_VIOLATIONS"
  exit 1
fi
echo "✓ Passed: No hardcoded colors in UI code"
echo ""

echo "Check 3: Verifying no hex codes in Theme.kt..."
THEME_HEX=$(grep -nE "Color\(0x[A-Fa-f0-9]{6,8}\)" "$THEME_FILE" || true)

if [ -n "$THEME_HEX" ]; then
  echo "❌ FAILED: Hex codes found in Theme.kt (move them to ColorPalette.kt as named tokens):"
  echo "$THEME_HEX"
  exit 1
fi
echo "✓ Passed: No hex codes in Theme.kt"
echo ""

echo "Check 4: Verifying file integrity..."
if [ ! -f "$HASH_FILE" ]; then
  echo "❌ FAILED: $HASH_FILE not found. Run: sha256sum $PALETTE_FILE $THEME_FILE > $HASH_FILE"
  exit 1
fi

for f in "$PALETTE_FILE" "$THEME_FILE"; do
  if ! grep -q "  $f\$" "$HASH_FILE"; then
    echo "❌ FAILED: $f is not covered by $HASH_FILE"
    echo "Run: sha256sum $PALETTE_FILE $THEME_FILE > $HASH_FILE"
    exit 1
  fi
done

if sha256sum -c "$HASH_FILE" >/dev/null 2>&1; then
  echo "✓ Passed: SHA-256 hashes verified"
else
  echo "❌ FAILED: SHA-256 hash mismatch"
  echo "Current hashes:"
  sha256sum "$PALETTE_FILE" "$THEME_FILE"
  echo ""
  echo "Expected hashes:"
  cat "$HASH_FILE"
  exit 1
fi
echo ""

echo "=== All checks passed ✓ ==="
