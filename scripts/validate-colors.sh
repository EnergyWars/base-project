#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
UIKIT_SRC="$PROJECT_ROOT/uikit/src/main/java/com/wafflehq/uikit"
APP_SRC="$PROJECT_ROOT/app/src/main/java/com/wafflehq/base"
PALETTE_KT="$UIKIT_SRC/theme/Palette.kt"
THEME_KT="$UIKIT_SRC/theme/Theme.kt"
TYPE_KT="$UIKIT_SRC/theme/Type.kt"

ERRORS=0
WARNINGS=0

echo "=== WaffleHQ Color System Validation ==="
echo ""

# 1. Check that color tokens (Sapphire, Emerald, etc.) are not imported/used outside Palette.kt/Theme.kt
echo "1. Checking direct color token usage..."
TOKEN_USAGE=$(grep -r "\(Sapphire\|Aquamarine\|Amethyst\|Citrine\|Garnet\|Graphite\|DarkBackground\|LightBackground\|OnSurface\(Dark\|Light\)\|OutlineDark\|OutlineLight\)" \
  "$UIKIT_SRC" "$APP_SRC" --include="*.kt" \
  | grep -v "$PALETTE_KT" | grep -v "$THEME_KT" | grep -v "/test/" \
  | grep -v "Section03Ramps.kt" || true)

if [ -z "$TOKEN_USAGE" ]; then
  echo -e "${GREEN}✓${NC} No direct color token usage outside Palette.kt/Theme.kt"
else
  echo -e "${RED}✗${NC} Direct color token usage found outside Palette.kt/Theme.kt:"
  echo "$TOKEN_USAGE" | cut -d: -f1 | sort -u
  ERRORS=$((ERRORS + 1))
fi

# 2. Check for hardcoded hex colors outside the palette definition
echo ""
echo "2. Checking for hardcoded hex colors..."
HEX_COLORS=$(grep -r "Color(0x[0-9A-Fa-f]" "$UIKIT_SRC" "$APP_SRC" --include="*.kt" \
  | grep -v "$PALETTE_KT" | grep -v "$THEME_KT" | grep -v "Type.kt" \
  | grep -v "/test/" | grep -v "ColorCanvasPicker.kt" || true)

if [ -z "$HEX_COLORS" ]; then
  echo -e "${GREEN}✓${NC} No hardcoded hex colors outside Palette.kt/Theme.kt"
else
  echo -e "${RED}✗${NC} Hardcoded hex colors found:"
  echo "$HEX_COLORS"
  ERRORS=$((ERRORS + 1))
fi

# 3. Check that all required color ramps exist in Palette.kt
echo ""
echo "3. Checking for required color ramps..."
REQUIRED_HUES=("Sapphire" "Aquamarine" "Amethyst" "Emerald" "Citrine" "Garnet" "Graphite")

MISSING=0
for HUE in "${REQUIRED_HUES[@]}"; do
  if ! grep -q "\"${HUE}\"" "$PALETTE_KT"; then
    echo -e "${RED}✗${NC} Missing ramp: ${HUE}"
    MISSING=$((MISSING + 1))
  fi
done

if [ $MISSING -eq 0 ]; then
  echo -e "${GREEN}✓${NC} All required color ramps present"
else
  ERRORS=$((ERRORS + MISSING))
fi

# 4. Check that fonts are properly configured
echo ""
echo "4. Checking font configuration..."

if grep -q "GeistSans" "$TYPE_KT"; then
  echo -e "${GREEN}✓${NC} Geist Sans font configured"
else
  echo -e "${RED}✗${NC} Geist Sans font not found"
  ERRORS=$((ERRORS + 1))
fi

if grep -q "GeistMono" "$TYPE_KT"; then
  echo -e "${GREEN}✓${NC} Geist Mono font configured"
else
  echo -e "${RED}✗${NC} Geist Mono font not found"
  ERRORS=$((ERRORS + 1))
fi

# 5. Check for invalid Color() usage
echo ""
echo "5. Checking for invalid Color() usage patterns..."
INVALID_COLOR_USAGE=$(grep -r "Color\.Red\|Color\.Green\|Color\.Blue\|Color\.Yellow\|Color\.Gray\|Color\.DarkGray\|Color\.LightGray" \
  "$UIKIT_SRC" "$APP_SRC" --include="*.kt" | grep -v "/test/" | grep -v "Color\.Transparent\|Color\.White\|Color\.Black" || true)

if [ -z "$INVALID_COLOR_USAGE" ]; then
  echo -e "${GREEN}✓${NC} No invalid Color.* usage (Color.Red, Color.Green, etc.)"
else
  echo -e "${RED}✗${NC} Invalid Color.* usage found:"
  echo "$INVALID_COLOR_USAGE"
  ERRORS=$((ERRORS + 1))
fi

# Summary
echo ""
echo "=== Summary ==="
if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
  echo -e "${GREEN}✓ All validations passed!${NC}"
  exit 0
elif [ $ERRORS -eq 0 ]; then
  echo -e "${YELLOW}⚠ Passed with $WARNINGS warning(s)${NC}"
  exit 0
else
  echo -e "${RED}✗ Validation failed with $ERRORS error(s) and $WARNINGS warning(s)${NC}"
  exit 1
fi
