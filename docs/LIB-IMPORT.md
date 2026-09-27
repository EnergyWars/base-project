# Library aus dem Basisprojekt in ein anderes Projekt einbinden

Das Verzeichnis `lib/` enthält die gesamte generische Library (Kotlin/Compose, package `com.wafflehq.lib.*`) als eigenständige Gradle-Module. Es ist eine 1:1-Übernahme des Library-Teils von `../periodical/lib` (ohne die kalender-/todo-spezifischen Module `domaincore` und `todocore`, ohne Code-Kommentare) und wird mit `scripts/sync-lib.sh` aktuell gehalten. Dateien in `lib/` werden nie von Hand geändert.

## Module

| Modul | Inhalt |
|---|---|
| `:lib:ui-core` | Buttons, Karten, Dialoge, Chips, Listen, Menüs, Tabs, Sheets, Gesten, CSV, Zeitformate, Offscreen-Rendering |
| `:lib:navigation` | Settings-Bausteine, App-Shell (Top-Navigation, Seitenmenü), Editor-Gerüst |
| `:lib:settings` | Farbsystem (Palette, Token-Registry, Resolver, Persistenz, Editor-UI), Verschlüsselung, Drive-Backup, Datenschutz/Lizenzen, Onboarding |
| `:lib:folders` | Ordnerbaum, Drag & Drop, Ordner-UI |
| `:lib:entrylock` | Eintrags-Sperre (Biometrie) |
| `:lib:quickpicker` | Schnelle Datums-/Uhrzeiteingabe |
| `:lib:textarea` | Tastaturbewusstes Mehrzeilenfeld |
| `:lib:drafts` | Entwurfs-Autosave |
| `:lib:modules` | Feature-Modul-Registry, Berechtigungskatalog |
| `:lib:maintenance` | Gebündelte Hintergrundaufgaben (WorkManager) |
| `:lib:database` | SQLCipher-Verschlüsselungskern |
| `:lib:backupcore` | Backup-Aufbewahrung und Versionsprüfung |
| `:lib:prefsbackup` | Preferences-Backup-Codec |
| `:lib:pdf` | PDF-Export, Vorschau, Wasserzeichen |
| `:lib:charts` | Linien-, Balken- und Radar-Diagramme |
| `:lib:media` | Foto-/Audio-Ablage mit EXIF-Bereinigung |
| `:lib:diagnostics` | Diagnose-Logger, Freeze-Erkennung, Crash-Logger |
| `:lib:notifications` | Benachrichtigungskanäle, Sperrbildschirm-Schutz |
| `:lib:astronomy` | Sonnen-/Mondberechnung |
| `:lib:qr` | QR-Code-Erzeugung |

## In ein anderes Projekt einbinden

1. `gradle/libs.versions.toml` aus dem Basisprojekt übernehmen (die Module benutzen genau diese Aliase) und im Root-`build.gradle.kts` die Plugins `android.library`, `kotlin.jvm`, `kotlin.android`, `kotlin.compose`, `kotlin.serialization`, `aboutlibraries` mit `apply false` deklarieren (siehe `build.gradle.kts` des Basisprojekts, inklusive `subprojects { tasks.withType<Test> … }`).
2. In der `settings.gradle.kts` des Zielprojekts:

```kotlin
extra["wafflehqLibDir"] = file("../base-project/lib").absolutePath
apply(from = file("../base-project/lib-modules.settings.gradle.kts"))
```

Das bindet alle Module als `:lib:<name>` ein (Projektverzeichnis bleibt im Basisprojekt). Alternativ einzelne Module per `include(":lib:ui-core")` und `project(":lib:ui-core").projectDir = file("../base-project/lib/ui-core")`.

3. In der App: `implementation(project(":lib:ui-core"))` usw. Ein Beispiel für die komplette Verdrahtung (Theme, Farb-Tokens, Settings, Navigation) ist die App im Basisprojekt (`app/`).

## Aktualisieren

`scripts/sync-lib.sh [Pfad-zu-periodical]` kopiert `lib/`, die Versionskatalog-Datei und die Modul-Liste in `settings.gradle.kts` neu aus Periodical und entfernt dabei die Code-Kommentare (`scripts/strip_comments.py`). Danach `python3 scripts/check_imports.py lib` ausführen.
