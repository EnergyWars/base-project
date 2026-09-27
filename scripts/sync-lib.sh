#!/bin/bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE_DIR="${1:-$PROJECT_DIR/../periodical}"
EXCLUDED_MODULES=("domaincore" "todocore")

if [ ! -d "$SOURCE_DIR/lib" ]; then
  echo "Source lib directory not found: $SOURCE_DIR/lib" >&2
  exit 1
fi

is_excluded() {
  for excluded in "${EXCLUDED_MODULES[@]}"; do
    [ "$1" = "$excluded" ] && return 0
  done
  return 1
}

rm -rf "$PROJECT_DIR/lib"
mkdir -p "$PROJECT_DIR/lib"

for module_path in "$SOURCE_DIR"/lib/*/; do
  module="$(basename "$module_path")"
  if is_excluded "$module"; then
    continue
  fi
  rsync -a --exclude 'build/' --exclude '.gradle/' --exclude '*.iml' "$module_path" "$PROJECT_DIR/lib/$module/"
done

python3 "$PROJECT_DIR/scripts/strip_comments.py" "$PROJECT_DIR/lib"

cp "$SOURCE_DIR/gradle/libs.versions.toml" "$PROJECT_DIR/gradle/libs.versions.toml"
python3 "$PROJECT_DIR/scripts/strip_comments.py" "$PROJECT_DIR/gradle/libs.versions.toml" || true

{
  echo "pluginManagement {"
  echo "    repositories {"
  echo "        google()"
  echo "        mavenCentral()"
  echo "        gradlePluginPortal()"
  echo "    }"
  echo "}"
  echo "dependencyResolutionManagement {"
  echo "    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)"
  echo "    repositories {"
  echo "        google()"
  echo "        mavenCentral()"
  echo "    }"
  echo "}"
  echo "rootProject.name = \"BaseApp\""
  echo "include(\":app\")"
  for module_path in "$PROJECT_DIR"/lib/*/; do
    echo "include(\":lib:$(basename "$module_path")\")"
  done
} > "$PROJECT_DIR/settings.gradle.kts"

(cd "$PROJECT_DIR" && sha256sum lib/settings/src/main/kotlin/com/wafflehq/lib/settings/colors/ColorPalette.kt app/src/main/java/com/wafflehq/base/ui/theme/Theme.kt > theme-hashes.sha256)

echo "Synced $(ls "$PROJECT_DIR/lib" | wc -l) library modules from $SOURCE_DIR"
