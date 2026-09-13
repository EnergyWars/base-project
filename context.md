# context.md – Basisprojekt

Diese Datei beschreibt den **aktuellen Stand** (keine Historie). Bei
Widersprüchen hat `FEATURES.md` Vorrang. Nach jeder Funktions-Änderung pflegen.

## Aufbau jedes Projektes

- **FEATURES.md**: im Projektstamm, selbstpflegend; enthält alle Features
  vollständig. Bei jeder Feature-Änderung nach Abschluss aktualisieren. Nur bei
  Bedarf nachschlagen.
- **.gitignore**: im Projektstamm; enthält immer `dev/*`, `java_pid*`, `*.hprof`.
  Laufend befüllen.
- **context.md**: zuerst lesen; nur aktueller Stand, kein Verlauf; Features aus
  `FEATURES.md` mit Status + zugehörigen Dateien; nach jeder Änderung kürzen.
- **CLAUDE.md**: klein halten; Hinweise zur Pflege von FEATURES.md, .gitignore
  und context.md.

## Stack

Kotlin 2.0, AGP 8.9, JVM 17, compileSdk 35, minSdk 26. Jetpack Compose
Material 3, Hilt, Room, DataStore (nur im App-Modul — `:uikit` ist frei von
Hilt/Room/DataStore, mit Ausnahme von `uikit/database`, das bewusst
SQLCipher/androidx.sqlite nutzt, da Verschlüsselung sein eigentlicher Zweck
ist). `gradle build`/`assembleDebug`/jeder `./gradlew`-Aufruf nie ohne
explizite Erlaubnis ausführen. **`:uikit` läuft zuletzt sauber durch**
(Kompilierung Debug/Release, 611+ Tests). **`:app` schlägt aktuell bei
`kspDebugKotlin`/`kspReleaseKotlin` fehl** (`FeatureFilesRepository` wird in
`di/AppModule.kt`/`ui/settings/SettingsViewModel.kt` referenziert, ist aber
nirgends definiert) — vorbestehend, nicht durch die Modal-/Header-Änderungen
verursacht, noch offen.

## Modul-Struktur: `:uikit` (Library) + `:app` (Beispiel-App)

**`:uikit`** (`com.wafflehq.uikit`, Android-Library) ist das eigenständig
importierbare Design-System- und Utility-Paket; **`:app`** ist die
Beispiel-App, die `:uikit` per `implementation(project(":uikit"))` konsumiert
und alle Bausteine vorführt. Ein fremdes Projekt bindet `:uikit` per
`includeBuild`/Modul-Kopie ein, ohne dessen Code anzufassen.

### `:uikit` – Theme (extern themebar ohne Code-Änderung)

- `theme/Palette.kt`: `ColorRamp` (9 Tones "10".."90"), `WafflePalette`
  (7 Rollen-Ramps + Light/Dark-Surfaces), `WafflePalette.Default`.
- `theme/TypeScale.kt`: `AppTypeScale`/`TypeStyleSpec` (15 M3-Stufen),
  `AppTypeScale.Default`, `buildTypography(scale, fontFamily)`.
- `theme/Theme.kt`: `AppTheme(darkTheme, palette = LocalWafflePalette.current,
  typeScale = LocalAppTypeScale.current, content)` baut `AppColors`/
  `AppTokens`/M3-`ColorScheme`/`Typography` zur Laufzeit. `AppTheme`-Objekt:
  `.colors`/`.tokens`/`.extendedColors`/`.palette`/`.typeScale`/`.colorRamps`.
  Hausschrift bleibt Geist (`Type.kt`, TTFs in `uikit/src/main/res/font/`) —
  themebar sind Größen/Gewichte, nicht die Fontfamilie.
- `theme/AppTokens.kt`/`AppShapes.kt`/`ThemeMode.kt`: Button/Chip/Card/
  TextField/Banner/Badge-Tokens, Radius/Spacing, `ThemeMode`-Enum
  (persistenzfrei).

### `:uikit` – Farbeinstellung inkl. Farbpicker (`color/`)

Generalisierter Port aus `../periodical`: `ColorCanvasPicker.kt` (SV-Panel +
Hue-/Alpha-Slider), `ColorSpaceConversions.kt` (RGB/HSL/Hex),
`RampGenerator.kt` (`rampFromAccent` — eine Akzentfarbe → vollständige
9-Tone-Ramp über feste Lightness-Kurve, dadurch bleiben alle Showcase-
Beispiele nach Farbänderung automatisch stimmig, da sie aus
`AppTheme.colorRamps` lesen), `WafflePaletteState.kt` (hoistbarer Halter mit
`setRoleAccent`/`resetRole`/`resetAll`/`replace`/`syncFromExternal`,
`onPaletteChanged`-Callback für Persistenz durch die Host-App),
`PaletteJson.kt` (Export/Import als JSON), `ColorSettingsScreen.kt` (fertiger
Editor: 7 Rollen mit Swatch → Picker-Dialog, „Alle zurücksetzen“).

### `:uikit` – Komponenten & Showcase

- `components/`: `AppBadge`, `AppBanner`, `AppButton`, `AppCard`, `AppChip`,
  `AppDialog` (themed `AlertDialog`-Wrapper: `AppRadius.dialog`-Shape,
  `AppTheme.colors.surface`/`onSurface`-Tokens; dazu
  `AppDialogConfirmButton`/`AppDialogDismissButton` als `AppButton`-Presets
  — Filled/Rolle bzw. Text/Neutral — für konsistente Modal-Buttons; alle
  Dialoge in `folders/`, `color/`, `quickpicker/` nutzen jetzt `AppDialog`
  statt roher `AlertDialog`/`TextButton`), `AppHeader`/`AppScaffold`
  (Tab-Leiste identisch zu periodicals `AppTopNavBar`: 64×32 dp Pill-
  Indikator statt Vollflächen-Highlight, Farben aus `navigation/AppNavColors`),
  `AppIconButton`, `AppSlider`, `AppTextField`, `SettingsUi.kt`,
  `DisplaySettingsContent.kt`.
- `showcase/`: alle 33 `SectionNN*.kt` (IDs `1a.1`…`33a.*`, Schema
  `<Sektion><Gruppe>.<Nr>`), `ElementInspector.kt` mit
  **`ElementInspectorHost(enabled = false, content)`** — Default **aus** für
  fremde Importeure; die Beispiel-App schaltet es in `HomeScreen.kt` mit
  `enabled = true` ein.

### `:uikit` – Portierte Periodical-Libraries (`../periodical/libraries.md`)

Alle als generisch bewerteten Teile wurden nach `com.wafflehq.uikit.<name>`
übernommen (Domänenspezifisches aus periodicals Kalender-App blieb dort):

| Paket | Inhalt |
|---|---|
| `astronomy/` | Sonnen-/Mond-Berechnungen (Golden/Blue Hour, Mondphasen) |
| `qr/` | `QrBitmapGenerator` (zxing-Wrapper) |
| `maintenance/` | `MaintenanceTaskRunner`/`MaintenanceTask` (geordnete Task-Ausführung mit Fehlerbehandlung) |
| `drafts/` | `DraftRepository`/`DraftAutosaveEffect` (Autosave-Mechanismus, framework-frei) |
| `modules/` | `FeatureModule`/`FeatureModuleRegistry`/`ModulePermissionSpec`/`CalendarDayMarker` (Plugin-Erweiterungspunkt-Muster) |
| `entrylock/` | `EntryAuthenticator` (BiometricPrompt), `EntryLockController` (State-Machine), `entrylock/ui/*` (Lock-Screen-UI) |
| `pdf/` | `PdfPageState`/`PdfWatermark`/`PdfDrawHelpers` (Wasserzeichentext jetzt Parameter statt periodical-String-Resource) |
| `folders/` | `FolderTree`/`FolderDrop`/`FolderDeletionAction`, `folders/ui/*` (Ordnerbaum mit Drag & Drop) |
| `navigation/` | `SettingsHomePage`, `EditorScaffold`, `FullScreenSubPage`, `AppNavigationShell`, `AppNavColors` (redundante Settings-Listen-Primitive gegenüber `components/SettingsUi.kt` wurden bewusst NICHT übernommen) |
| `quickpicker/` | `QuickDateInputDialog`/`QuickTimeInputDialog`/`TimePickerField` (Ziffern-Eingabe-Picker statt System-Picker) |
| `database/` | SQLCipher-verschlüsselte SQLite-Öffnung/Migration (`DatabaseOpenPlanner`, `crypto/*`, `conversion/*`) — einzige Ausnahme von „kein Room/Hilt/DataStore“, da Verschlüsselung der Zweck ist |
| `textarea/` | `KeyboardAwareTextArea`/`TextAreaAutoScroll` (IME-bewusstes Mehrzeilen-Textfeld) |

### `:uikit` – Tests

`uikit/src/test/…`: 611+ Tests (JUnit4 + Robolectric für Compose-UI/`org.json`/
SQLite-Fälle), decken die komplette reine Logik sowie die wichtigsten
Compose-Komponenten ab (inkl. `ElementInspectorHost(enabled=…)`-Verhalten).
`:uikit`s `release`-Build-Variante hat
`enableUnitTest = false` (in `uikit/build.gradle.kts`), da
`androidx.compose.ui:ui-test-manifest` bewusst nur `debugImplementation` ist
und Compose-UI-Tests einen Debug-Manifest-Overlay brauchen.

### `:app` – Beispiel-App (Konsument von `:uikit`)

| Feature | Status | Dateien |
|---|---|---|
| Home-Screen – Showcase aller `:uikit`-Elemente (33 Sektionen) | fertig | `ui/home/HomeScreen.kt` |
| Element-Inspektor eingeschaltet | fertig | `HomeScreen.kt`: `ElementInspectorHost(enabled = true)` |
| Farbpalette-Einstellung (Settings → „Farbpalette“) | fertig | `ui/settings/ColorSettingsRoute.kt`, `SettingsViewModel.paletteState`, `SettingsRepository.paletteJson` (DataStore-persistiert), `MainActivity.kt` |
| App-Header/Drawer/Beispielseiten 1–3 | fertig | `ui/components/AppDrawer.kt` (app-spezifisch), `ui/example/ExampleScreen.kt` |
| Settings-Listenseite + Anzeige-Unterseite | fertig | `ui/settings/*.kt` |
| Feature-Liste (Markdown aus `features/*.md`) | **kaputt** — `FeatureFilesRepository` (referenziert in `di/AppModule.kt`, `ui/settings/SettingsViewModel.kt`) existiert nicht im Code; `ui/features/`/`data/features/` fehlen komplett. `:app:kspDebugKotlin`/`kspReleaseKotlin` schlagen deshalb fehl. Vorbestehend, nicht Teil dieser Änderung. | — |
| Navigation | fertig | `ui/navigation/AppNavHost.kt` (Routen inkl. `settings_colors`, `library_examples`) |
| DI / DB / App | fertig | `di/AppModule.kt`, `data/db/AppDatabase.kt`, `BaseApp.kt`, `MainActivity.kt` |
| Bibliotheken-Beispielseite (Drawer → „Bibliotheken“) | fertig | `ui/library/LibraryExamplesScreen.kt` — je ein funktionierendes Beispiel für alle 11 portierten `:uikit`-Libraries (astronomy/qr/maintenance/drafts/modules/entrylock/pdf/folders/navigation/quickpicker/database/textarea) |

## Status

Alle Punkte aus dem ursprünglichen `/goal`-Auftrag sind umgesetzt: `:uikit`
ist per Gradle importierbar, Elemente sind über IDs auffindbar, alle
generischen Periodical-Libraries sind portiert, Farben/Schriftgrößen sind
extern themebar, die Farbeinstellung inkl. Picker ist Teil des Pakets, alles
ist beispielhaft in `:app` eingebunden, der Element-Inspektor ist per
`enabled`-Flag abschaltbar (Default aus). `:uikit` baut/testet vollständig
grün (Kompilierung Debug/Release, 611+ Unit-Tests). `:app` baut aktuell
**nicht** durch — siehe `FeatureFilesRepository`-Lücke oben. `verify-theme.sh`/
`scripts/validate-colors.sh` sind an die neue Modul-Struktur angepasst.
