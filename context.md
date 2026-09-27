# context.md – Basisprojekt

Nur aktueller Stand, keine Historie. Bei Widersprüchen hat `CLAUDE.md` Vorrang. Nach jeder Funktions-Änderung pflegen.

## Aufbau jedes Projektes

- `FEATURES.md` im Projektstamm: alle Features, nach Abschluss eines Features aktualisieren, nur bei Bedarf nachschlagen.
- `.gitignore` im Projektstamm, laufend befüllen (enthält `dev/*`, `java_pid*`, `*.hprof`).
- `context.md`: zuerst lesen, nur aktueller Stand, nach jeder Änderung kürzen.
- `CLAUDE.md`: klein halten, keine Feature-Beschreibungen.

## Stack

Kotlin 2.3, AGP 8.13, JVM 17, compileSdk/targetSdk 36, minSdk 26. Compose Material 3, Hilt, Room, DataStore (nur `:app`). Versionen in `gradle/libs.versions.toml` (Kopie aus Periodical). Gradle nie ohne ausdrückliche Erlaubnis ausführen.

## Module

- `:app` (`com.wafflehq.base`): Beispiel-App.
- `:lib:*` (20 Module, `com.wafflehq.lib.*`): 1:1-Kopie von `../periodical/lib` ohne `domaincore`/`todocore`, kommentarfrei. Erzeugt durch `scripts/sync-lib.sh` (rsync, `scripts/strip_comments.py`, Katalog, `settings.gradle.kts`, `theme-hashes.sha256`). Nie von Hand ändern.
- Einbindung in andere Projekte: `docs/LIB-IMPORT.md`, `lib-modules.settings.gradle.kts`.

Lib-Modul-Abhängigkeiten: `ui-core` ← `navigation`, `folders`, `entrylock`, `quickpicker`, `textarea`, `pdf`, `settings`; `settings` ← `navigation`, `database`, `backupcore`. Alle übrigen Module sind eigenständig.

## Skripte

| Datei | Zweck |
|---|---|
| `scripts/sync-lib.sh` | Lib aus Periodical neu übernehmen |
| `scripts/strip_comments.py` | Kommentare aus `.kt`/`.kts`/`.xml` entfernen |
| `scripts/check_imports.py` | `com.wafflehq.*`-Imports gegen `lib/` und `app/` prüfen (Ersatz für Compiler-Lauf; Fehlalarm bei Extension-Funktionen mit Receiver-Typ möglich) |
| `verify-theme.sh`, `theme-hashes.sha256` | Theme-Regeln und Prüfsummen von `ColorPalette.kt`/`Theme.kt` |
| `scripts/validate-colors.sh` | Farbregeln-Prüfung |

## `:app`

| Bereich | Status | Dateien (unter `app/src/main/java/com/wafflehq/base/`) |
|---|---|---|
| Theme, Rollenfarben, Typografie | fertig | `ui/theme/` (`Theme.kt` = `BaseAppTheme`, `AppTheme.kt` = 7 Rollen + `colorRamps`, `AppThemedContent.kt`, `ColorTokenAccess.kt`, `Type.kt`, `Shape.kt`) |
| Farb-Token-Katalog (Global, Success) | fertig | `domain/colortheme/` |
| Farb-Persistenz | fertig | `di/ColorThemeModule.kt` (Override-Store, Registry, Export) |
| Einstellungen (Liste, Anzeige, Farben, Farb-Kategorien) | fertig | `ui/settings/` |
| Feature-Liste (Markdown aus `features/*.md`) | fertig | `data/features/FeatureFilesRepository.kt`, `ui/features/`, Gradle-Task `syncFeatureFiles` |
| Navigation, Seitenmenü, Titelleiste | fertig | `ui/navigation/AppNavHost.kt`, `ui/components/AppDrawer.kt`, `AppHeaderScaffold.kt` |
| Showcase (33 Sektionen) + Element-Inspektor | fertig | `ui/home/HomeScreen.kt`, `ui/showcase/` (IDs: `SHOWCASE-ELEMENT-IDS.md`) |
| Beispielseiten 1–3 | fertig | `ui/example/ExampleScreen.kt` |
| Bibliotheken-Beispiele (24 Demos, alle Lib-Module) | fertig | `ui/library/LibraryExamplesScreen.kt`, `ui/library/demos/` |
| DI, DB, Activity | fertig | `di/AppModule.kt`, `data/db/AppDatabase.kt`, `data/settings/SettingsRepository.kt`, `BaseApp.kt`, `MainActivity.kt` |

Strings: `res/values*/strings.xml` (App), `showcase_strings.xml`, `library_examples_strings.xml` (`libex_*`). Fonts: `res/font/geist_*.ttf`, `geist_mono_*.ttf`.

## Tests

`app/src/test/` (JUnit4, Robolectric, Compose-Test) für Katalog, Theme, Repositories, ViewModels, Navigation, Showcase-Bausteine und alle Library-Demos; Lib-Module bringen ihre eigenen Tests mit. Bisher nicht ausgeführt (Gradle-Lauf steht aus).

## Offene Punkte

- Alter Ordner `uikit/` liegt noch im Projekt (nicht in `settings.gradle.kts` eingebunden, ersetzt durch `lib/` + `app/ui/showcase`); zum Löschen freigeben.
- Entry-Lock-Demo: `MainActivity` ist `ComponentActivity`, echte Biometrie bräuchte `FragmentActivity`.
- PDF-Teilen in der Demo braucht einen `FileProvider` im Manifest.
