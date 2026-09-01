# Basisprojekt

Dieses Verzeichnis ist die **Basis-App** für WaffleHQ-Android-Projekte. Es ist die Vorlage, die per Kopieren in ein neues Projekt zur leeren Start-Codebase wird – mit allen wichtigen Architektur-Bausteinen schon eingebaut.

## Architektur (bereits enthalten)

- **Sprache/Build:** Kotlin 2.0, AGP 8.9, JVM 17, `compileSdk 35`, `minSdk 26`, `targetSdk 35`.
- **UI:** Jetpack Compose Material 3, Single-Activity, Compose Navigation.
- **DI:** Hilt (`@HiltAndroidApp` auf `BaseApp`, Module in `di/AppModule.kt`).
- **DB:** Room (`data/db/AppDatabase.kt` mit Placeholder-Entity – Entities ersetzen, Version hochzählen).
- **Settings/Preferences:** DataStore Preferences (`data/settings/SettingsRepository.kt`) inkl. Theme-Mode.
- **State:** `StateFlow` + `collectAsStateWithLifecycle()` (MVVM, ViewModels via `@HiltViewModel`).
- **i18n:** Deutsch zuerst (`values-de/strings.xml`), Englisch ist Fallback (`values/strings.xml`). Alle UI-Strings gehen über `stringResource(R.string.…)`, **nie** hartcodieren.
- **Navigation:** `ui/navigation/AppNavHost.kt` mit `Routes`-Objekt; Home- und Settings-Route vordefiniert.
- **TopAppBar:** Auf jedem Screen vorhanden; Settings ist über Zahnrad-Icon auf Home erreichbar.
- **Theme-Toggle:** Settings-Screen mit Radio-Group System / Light / Dark, persistiert über DataStore. `MainActivity` löst den `ThemeMode` zur Laufzeit auf.

## Design

Verbindlich ist der Skill `wafflehq-design`. **Bei jeder UI-Änderung gelten:**

- **Farben:** Nur Tokens aus `ui/theme/Color.kt`. Die fünf Hue-Ramps sind eingebaut: **Sapphire** (Primary), **Emerald** (Secondary), **Amethyst** (Tertiary), **Citrine** (Warning), **Garnet** (Error). Niemals neue Hex-Werte direkt im Code – stattdessen Ramp erweitern.
- **Material-Tokens benutzen:** `MaterialTheme.colorScheme.primary` etc., nicht hardcoded Color.
- **Keine Dynamic Color / kein Material You.** Die App sieht auf jedem Gerät gleich aus.
- **Corner Radii:** Cards 12 dp, Buttons 20–24 dp, Sheets/Dialogs 28 dp Top, Outlined-TextField 4 dp.
- **Grid:** 8 dp; Card-Padding 12 / 16 dp; Section-Gaps 16 dp; Chip-Spacing 8 dp.
- **Shadows minimal:** 1 dp tonal elevation, keine großen Drop-Shadows, keine Gradienten, kein Glas/Blur.
- **Icons:** `androidx.compose.material.icons.Icons.*` (Core oder `material-icons-extended`). Keine eigenen SVGs außer Launcher.
- **Typografie:** Quicksand (Hausschrift laut `/wafflehq-design`). Sobald `Quicksand-{Light,Regular,Medium,SemiBold,Bold}.ttf` unter `res/font/` liegen, `WaffleHqFontFamily` in `ui/theme/Type.kt` darauf umstellen. Bis dahin fällt die App auf `FontFamily.Default` zurück. M3-Default-Skala; Display/Headline 700, Title/Label 600, Body 400.
- **Copy-Stil (Sentence case, terse, kein Marketing, keine Ausrufezeichen):**
  - "Add event", nicht "Add Event!"
  - "No items yet.\nTap + to add one." statt "Welcome! Start by tapping the plus button."
  - Confirms mit `?`: "Delete entry?"
  - Inline-Symbole erlaubt: `✓ ✗ · … ± ≈ Ø`.
- **Sprache:** Deutsch ist Erstsprache. Englisch ist Fallback. Jeder neue String **beide** `strings.xml` ergänzen.

Vor jeder UI-Arbeit den Skill `wafflehq-design` lesen (insbesondere `README.md` und `colors_and_type.css`).

## Was beim Kopieren als Vorlage anzupassen ist

1. `applicationId`, `namespace`, Package-Pfad (`com.wafflehq.base` → `com.wafflehq.<app>`).
2. `rootProject.name` in `settings.gradle.kts`.
3. `app_name` in beiden `strings.xml`.
4. Launcher-Icon (`mipmap-*`) ersetzen.
5. `PlaceholderEntity` durch echte Entities ersetzen, DB-Version hochziehen.
6. Eigene `FEATURES.md` und `context.md` anlegen (siehe globale `~/.claude/CLAUDE.md`).

## Build

`gradle build` oder `assembleDebug` **niemals** ohne explizite Aufforderung ausführen.
