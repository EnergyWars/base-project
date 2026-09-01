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
Material 3, Single-Activity, Compose Navigation, Hilt, Room, DataStore.
Hausschrift **Geist** (`res/font/`). `gradle build`/`assembleDebug` nie ohne
explizite Erlaubnis ausführen.

## Implementierungsstatus

| Feature | Status | Dateien |
|---|---|---|
| Home-Screen – Design-System-Showcase (33 Sektionen, inkl. App-Header + Settings-Listen/Detail-Mockups) | fertig | `ui/home/HomeScreen.kt`, `ui/theme/showcase/Section01..33*.kt`, `res/values/strings.xml`, `res/values-de/strings.xml` |
| Element-Inspektor (Doppeltipp → Popup mit Element-ID) | fertig | `ui/theme/showcase/ElementInspector.kt`, `ui/home/HomeScreen.kt`, Sektionen 02–05, `ShowcaseCommon.kt` |
| App-Header (persistent, 3 Buttons Menü/Start/Einstellungen) + `AppScaffold` | fertig | `ui/components/AppHeader.kt` |
| Navigation-Drawer (Burger) | fertig | `ui/components/AppDrawer.kt` |
| Beispielseiten 1–3 (Lorem ipsum) | fertig | `ui/example/ExampleScreen.kt` |
| Theme / Farben (7 Rampen, Light/Dark) | fertig | `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `ui/theme/AppTokens.kt`, `ui/theme/Type.kt`, `ui/theme/AppShapes.kt` |
| Wiederverwendbare Komponenten (Button/Chip/Card/Banner/Badge/IconButton/TextField/Slider) | vorhanden, vom Home-Screen aktuell nicht genutzt | `ui/components/*.kt` |
| Settings-Listenseite (Zeilen mit Chevron, eigener Zurück-Header) | fertig | `ui/settings/SettingsScreen.kt`, `ui/components/SettingsUi.kt` |
| Settings-Anzeige-Unterseite (Gruppen + Design-Dropdown + Beispiel-Slider/Switch) | fertig | `ui/settings/DisplaySettingsScreen.kt`, `ui/settings/DisplaySettingsContent.kt`, `ui/components/SettingsUi.kt`, `ui/settings/SettingsViewModel.kt`, `data/settings/*` |
| Feature-Liste (Markdown aus `features/*.md`) | fertig | `ui/features/*.kt`, `data/features/*` |
| Navigation (Drawer + Routen Home/Beispiel1–3/Settings/SettingsDisplay/Features) | fertig | `ui/navigation/AppNavHost.kt` |
| DI / DB / App | fertig | `di/AppModule.kt`, `data/db/AppDatabase.kt`, `BaseApp.kt`, `MainActivity.kt` |

## Hinweise zum Home-Screen & Header

- Jede der 33 Showcase-Sektionen liegt als eigenes Composable
  `ui/theme/showcase/SectionNN*.kt`; Sektion 22 zeigt App-Header-Mockups, 23/24 die
  Settings-Listen-/Detail-Seiten in „Phone“-Frames (`SettingsMock`). Die
  Mockups nutzen dieselben Bausteine aus `SettingsUi.kt`/`DisplaySettingsContent`.
  Ein früherer „Changelog“-Block (Was sich in v2.1 geändert hat) wurde entfernt.
  Sektion 17 (Banner) zeigt zusätzlich Dismiss-(✕)-, Inline-Action- und
  Filled-Varianten (IDs `17a.6`–`17a.9`).
- **Element-Inspektor** (`ui/theme/showcase/ElementInspector.kt`): Doppeltipp auf
  ein Element öffnet ein Popup mit dessen ID (`ElementInspectorHost` umschließt den
  Showcase in `HomeScreen`). `Modifier.inspectId(id, onClick = {})` (Doppelklick via
  `combinedClickable`) für Einzel-Elemente, `Modifier.inspectTap(id)` (ripple-frei
  via `detectTapGestures`, layoutneutral) für Gruppen-Container,
  `InspectSection("section.<slug>")` als Sektions-Fallback.
  **IDs folgen exakt den Codes des HTML-Showcase** (`wafflehq-showcase-v2.html`):
  Schema `<Sektion><Gruppen-Buchstabe>.<Element-Nummer>` (z. B. `3a.4`). Gruppen
  ohne Punkt (`3a`, `6a`, `22a` …), Elemente mit Punkt (`1a.1`, `3a.9`, `19a.16` …);
  Sektionen ohne Showcase-Gruppen nutzen die implizite Gruppe `a`. Die Codes werden
  in den `SectionNN*.kt` pro Element/Gruppe gesetzt (in Loops aus dem Index berechnet).
  Geteilte Settings-Komponenten (`SettingsUi.kt`, `DisplaySettingsContent`) tragen
  **optionale** Code-Parameter (Default `null`), damit die echten Settings-Screens
  unverändert bleiben. Token-Namen (`Sapphire40`) bleiben als IDs vermieden
  (verify-theme.sh erfüllt).
- Der **echte** App-Header (`AppHeader`/`AppScaffold`) ist die Kopfzeile der
  Hauptseiten (Home, Beispiel 1–3): links Burger → Drawer, mittig Start → Home,
  rechts Zahnrad → Settings. Aktiver Button nutzt Secondary-Container.
- Die **Settings-Seiten** sind ein Drill-in-Fluss mit eigener `SettingsTopBar`
  (Zurück-Pfeil + Titel), kein App-Header. Zahnrad/Drawer öffnen sie per Push
  (`navigate(... ){ launchSingleTop }`); Zurück per `popBackStack()`.
- Navigation über `ModalNavigationDrawer` in `AppNavHost`; Top-Level-Auswahl per
  `switchTo()` (launchSingleTop + popUpTo Home, saveState/restoreState), Settings
  per Push.
- **Slider** (`ui/components/AppSlider.kt`): `AppSlider` baut auf Material3-`Slider`
  mit eigenen `thumb`/`track`-Slots. 6 dp-Rail (`surfaceVariant`) mit gefülltem
  Fortschritt (`primary.accent`), schlanker 7×22 dp-Pill-Thumb mit 2 dp-`surface`-Rand,
  Stufen-Ticks (2×11 dp, `onSurface` 26 %) bei `steps > 0`, deaktiviert via Alpha 0,38.
  Wird von `SettingsSliderControl` und `Section15SliderProgress` genutzt.
- Settings-Bausteine in `ui/components/SettingsUi.kt`: `SettingsScaffold`,
  `SettingsTopBar`, `SettingsListContent`/`SettingsListRow`, `SettingsGroup`
  (+`SettingsGroupDivider`), `SettingsDropdownField`, `SettingsSliderControl`,
  `SettingsSwitchRow`. Tint-Kästchen via `lerp(surface/outline, akzent, …)`.
- Farben kommen ausschließlich aus `AppTheme.colors`/`AppTheme.colors.forRole(role)`.
- Farb-Tokens in `Color.kt` heißen ohne Prefix (z. B. `Sapphire40`, `Graphite80`,
  `DarkSurface`); der Name allein ist eindeutig. Das Mapping auf fachliche
  Rollen (`primary`, `secondary`, …) erfolgt in `Theme.kt`. `verify-theme.sh`
  prüft die Token-Isolation gegen diese prefixlosen Namen.
- Alle sichtbaren Texte liegen als Strings in beiden `strings.xml`
  (Deutsch + Englisch-Fallback). Keine UI-Strings im Code.
- Der Hell/Dunkel-Pill (Home) und das Design-Dropdown (Settings-Anzeige)
  schreiben `ThemeMode` über `SettingsViewModel`.
