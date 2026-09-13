# Features – Basisprojekt

Das Basisprojekt ist eine Vorlage. Die folgenden Features sind als minimaler, kopierbarer Stand vorhanden.

## `:uikit` – importierbares Design-System-Paket

Das Projekt besteht aus zwei Gradle-Modulen: **`:uikit`** (Android-Library,
`com.wafflehq.uikit`) enthält das komplette, eigenständig per Gradle
importierbare Design-System; **`:app`** ist die Beispiel-App, die `:uikit`
per `implementation(project(":uikit"))` einbindet und alle Bausteine
vorführt. Ein anderes Projekt bindet `:uikit` ein (z. B. per `includeBuild`
oder Modul-Kopie), ohne dessen Code anzufassen.

- **Alle Elemente sind unter ihrer Showcase-ID auffindbar** (`1a.1`…`33a.*`,
  siehe unten „Element-Inspektor“) — das gilt sowohl im Showcase selbst als
  auch für jede Komponente, die einen optionalen `code`/`inspectCode`-
  Parameter annimmt.
- **Farben und Schriftgrößen sind von außen themebar, ohne den Code von
  `:uikit` anzufassen**: `AppTheme(darkTheme, palette = WafflePalette(...),
  typeScale = AppTypeScale(...), content)` baut Farbschema, Rollen-Tokens und
  M3-`Typography` zur Laufzeit aus einer übergebenen `WafflePalette`
  (7 Rollen-Ramps à 9 Tones + Light/Dark-Surfaces) und `AppTypeScale`
  (15 M3-Textstufen mit Size/LineHeight/Weight/LetterSpacing). Wird nichts
  übergeben, gilt `WafflePalette.Default`/`AppTypeScale.Default` (die
  bisherigen WaffleHQ-Werte). Hausschrift bleibt fix Geist — themebar sind
  Größen/Gewichte/Letter-Spacing, nicht die Fontfamilie.
- **Farbeinstellung inkl. Farbpicker** (Paket `color/`, generalisierter Port
  aus `../periodical`): `ColorCanvasPicker` (SV-Panel + Hue-/Alpha-Slider,
  identisch zu periodical), `rampFromAccent(name, accent)` generiert aus
  **einer** gepickten Akzentfarbe eine vollständige, kontrastsichere 9-Tone-
  Ramp (feste Lightness-Kurve, Hue/Sat der Akzentfarbe) — dadurch bleiben
  **alle** Showcase-Beispiele nach einer Farbänderung automatisch stimmig,
  da sie ausnahmslos aus `AppTheme.colorRamps`/`AppTheme.colors` lesen.
  `WafflePaletteState` hält die aktuell bearbeitete Palette hoistbar/Compose-
  observabel (`setRoleAccent`/`resetRole`/`resetAll`/`replace`/
  `syncFromExternal`) und meldet Änderungen über einen `onPaletteChanged`-
  Callback an die Host-App zur Persistierung — `:uikit` selbst bleibt frei
  von Hilt/Room/DataStore. `WafflePalette.toJson()`/`wafflePaletteFromJson()`
  exportieren/importieren die Palette als JSON (Datei-I/O bleibt App-Sache).
  Fertiger `ColorSettingsScreen` listet alle 7 Rollen mit Swatch, öffnet pro
  Rolle den Picker, plus „Alle zurücksetzen“.
- **Einheitliches Modal-Design** (`components/AppDialog.kt`): themed
  `AlertDialog`-Wrapper (`AppRadius.dialog`-Shape 28 dp,
  `AppTheme.colors.surface`/`onSurface`/`onSurfaceVariant`, 1 dp
  Tonal-Elevation) plus `AppDialogConfirmButton`/`AppDialogDismissButton` als
  feste `AppButton`-Presets (Filled/Rolle für Bestätigen — `AppRole.Error` bei
  destruktiven Aktionen —, Text/Neutral für Abbrechen/Schließen). Alle
  bisherigen rohen `AlertDialog`/`TextButton`-Dialoge im Paket
  (`folders/ui/FolderSystemUi.kt`, `color/ColorSettingsScreen.kt`,
  `quickpicker/TimePickerField.kt`) nutzen jetzt `AppDialog`, damit jedes
  Modal dieselbe Form-/Farbsprache wie der Rest der App trägt.
- **Element-Inspektor abschaltbar**: `ElementInspectorHost(enabled = false,
  content)` — Default **aus**, damit ein fremdes Projekt beim Importieren
  keine Doppeltipp-ID-Popups bekommt, ohne `:uikit` anzupassen. Die
  Beispiel-App schaltet ihn in `HomeScreen.kt` explizit mit `enabled = true`
  ein.
- **Portierte Periodical-Libraries** (aus `../periodical/libraries.md`, alle
  generischen, nicht domänenspezifischen Teile): `astronomy` (Sonnen-/
  Mondberechnungen), `qr` (`QrBitmapGenerator`), `maintenance`
  (`MaintenanceTaskRunner`), `drafts` (`DraftAutosaveEffect`), `modules`
  (`FeatureModule`/`FeatureModuleRegistry`-Erweiterungspunkt), `entrylock`
  (Biometrie-Sperrbildschirm), `pdf` (`PdfPageState`/Wasserzeichen,
  Wasserzeichentext jetzt Parameter statt periodical-String), `folders`
  (Ordnerbaum mit Drag & Drop), `navigation` (`SettingsHomePage`/
  `EditorScaffold`/`AppNavigationShell` — Settings-Listen-Duplikate zu
  `components/SettingsUi.kt` bewusst nicht übernommen), `quickpicker`
  (Ziffern-Eingabe Date-/Time-Picker), `database` (SQLCipher-verschlüsselte
  SQLite-Öffnung/Migration), `textarea` (`KeyboardAwareTextArea`,
  IME-bewusstes Mehrzeilen-Textfeld). Jeweils package `com.wafflehq.uikit.*`,
  framework-frei (kein Hilt/Room/DataStore, außer `database` — dort ist
  Verschlüsselung der eigentliche Zweck). 611 Tests, `./gradlew build` läuft
  für beide Module fehlerfrei durch (Kompilierung, Tests, Lint, assemble).
- **Beispiel-Einbindung**: Alle 11 portierten Libraries sind über eine neue
  Seite „Bibliotheken“ (Drawer-Eintrag, `ui/library/LibraryExamplesScreen.kt`)
  in der Beispiel-App eingebunden — je ein funktionierendes Beispiel pro
  Library (u. a. lebender Sonnen-/Mondstand, QR-Code-Generator, Maintenance-
  Task-Runner, Autosave-Textfeld, Feature-Modul-Registry mit Schaltern,
  Biometrie-Reveal, PDF-Export, Ordnerbaum, Editor-Scaffold, Quick-Date-/Time-
  Picker, verschlüsselte Datenbank-Öffnung, tastaturbewusstes Textfeld).
  `./gradlew build` läuft für `:uikit` und `:app` vollständig durch
  (Kompilierung, Tests, Lint, assembleDebug/Release).

## Home-Screen – Design-System-Showcase (v2.1)

Der Home-Screen ist eine **1:1-Nachbildung** von `wafflehq-showcase-v2.1.html`
in Jetpack Compose und bindet ausschließlich die Theme-Farben aus
`ui/theme/Color.kt` (identische Hex-Werte wie das HTML). Die Sektionen sind als
eigenständige Composables in `ui/home/HomeScreen.kt` umgesetzt; Layout
(Panels, Pill-Buttons, Rampen, Listen, Banner …) ist per Hand nach dem
CSS des Showcase nachgebaut, damit die App-Ansicht dem HTML entspricht.
Aufbau (von oben nach unten):

- **Lede-Header** – Eyebrow (Mono), Titel (Display S) und Intro-Text.
- **Theme-Toggle-Leiste** – Pill-Umschalter Hell/Dunkel (schreibt via
  `SettingsViewModel`/DataStore sofort das ganze App-Theme) plus dekorativer
  „Theme herunterladen“-Pill.
- **1 · Typografie** – alle 15 M3-Stufen mit Name + Meta (Größe/Line-Height/
  Weight) in Mono.
- **2 · Schrift-Gewichte** – Light 300 bis Bold 700.
- **3 · Hue-Rampen** – sieben benannte Rampen (Sapphire, Aquamarine, Amethyst,
  Emerald, Citrine, Garnet, Graphite) je 9 Tones (10–90) mit Tone-Label und
  Hex (aus dem `Color`-Wert berechnet).
- **4 · Oberflächen & Outline** – Background, Surface, Surface variant,
  Outline, On-Surface, On-Surface variant.
- **5 · Rollen-Übersicht** – sieben Rollen-Karten (Accent-Fläche, On-Accent-
  Text, Kontrastwert + AA/AAA-Badge).
- **6 · Buttons** – pro Rolle ein Panel mit Filled, Tonal, Elevated, Outlined,
  Text plus Disabled-State (Custom-Pill-Buttons). Tonal-Variante hat einen
  1 dp Border in Rollenfarbe (Ramp-Ton 70 im Dark-, Ton 30 im Light-Theme).
- **7 · Floating Action Buttons** – Small/Standard/Large/Extended.
- **8 · Icon-Buttons** – Standard, Filled, Tonal, Outlined plus Disabled-Reihe
  und Error-/Success-/Tertiary-Beispiele.
- **9 · Chips** – Assist/Suggestion, Filter (Single-Choice, 4 Optionen),
  Input (selected/unselected mit Avatar + Close).
- **10 · Textfelder** – Outlined, Outlined + Leading-Icon, Error-State, Filled
  mehrzeilig, Disabled.
- **11 · Karten** – Filled, Elevated, Outlined.
- **12 · Listeneinträge** – ein-, zwei-, dreizeilig mit Leading-Avatar 44 dp.
- **13 · Auswahl-Steuerelemente** – Checkbox, Switch, Radio-Group.
- **14 · Segmented Buttons** – Single-Choice-Row Tag/Woche/Monat/Jahr.
- **15 · Slider & Fortschritt** – Eigener `AppSlider` (6 dp-Rail mit gefülltem
  Fortschritt, schlanker Pill-Thumb, Stufen-Ticks, deaktivierter Zustand)
  stufenlos/stufig/gesperrt; Progress linear & circular.
- **16 · Badges & Status-Pills** – Number-/Dot-Badge, Pills für alle Rollen.
- **17 · Banner** – Primary, Success, Warning, Error, Neutral; zusätzlich
  Dismiss-(✕)-, Inline-Action- und Filled-Variante (Codes `17a.6`–`17a.9`).
- **18 · Snackbar & Dialog** – Snackbar mit Action; eingebetteter
  Bestätigungs-Dialog (28 dp Radius), im Mockup-Stil identisch zum echten,
  wiederverwendbaren `components/AppDialog.kt` (siehe „`:uikit` –
  importierbares Design-System-Paket“).
- **19 · Icons** – 8er-Grid mit Material-Outlined-Symbols (Lucide-Pendants).
- **20 · Trennlinien** – Horizontal, Horizontal stark, Vertikal.
- **21 · Spacing & Radien** – 4/8/12/16/24/32 dp Grid plus Radien 4/8/12/16/28
  dp + Pill.
- **22 · App-Header (mobil)** – sechs „Phone“-Mockups, die den App-Header
  zeigen: Variante A (2-Seiten, links drittes Symbol), Variante B (mehrseitig,
  links Burger), Default (flach), Elevated, Aktive Seite + Badge sowie Burger
  ausgeklappt (Navigation-Drawer mit Scrim). Jeder Header hat drei gleich breite
  Buttons (Icon über Beschriftung) mit Trennlinie an der Unterkante; aktiver
  Button zeigt eine Pill hinter dem Icon (Primary-Container) statt einer
  Vollflächen-Highlight — gleicher Stil wie der echte `AppHeader`.
- **23 · Einstellungen — Listenseite** – „Phone“-Mockup mit Settings-Kopfzeile
  (Zurück-Pfeil + Titel) und voll klickbaren Zeilen (Hauptbezeichnung +
  optionaler Untertitel, rechts Chevron), getrennt durch Trennlinien.
- **24 · Einstellungen — Unterseite** – „Phone“-Mockup der „Anzeige“-Seite:
  Gruppen mit Mono-Label und leicht eingefärbtem Kästchen (Primary/Secondary/
  Tertiary-Tint). Gruppe „Allgemein“ mit Design-Dropdown, weitere Gruppen mit
  Beispiel-Regler (Slider) und Schalter (Switch). Im Showcase voll interaktiv
  über lokalen State.

Hintergrund nutzt `AppTheme.colors.background` – im Light-Modus das
WaffleHQ-Off-White `#F4F6FA`, im Dark-Modus `Abyss #080E18`.

### Element-Inspektor (Doppeltipp-ID)

Auf der Showcase-Seite lässt sich **jedes Element per Doppeltipp inspizieren**:
ein kleines Popup-Modal (`Dialog`) zeigt die **ID des Elements** in Mono-Schrift,
mit Buttons „Kopieren“ (Zwischenablage) und „Schließen“.

- Infrastruktur in `ui/theme/showcase/ElementInspector.kt`: `LocalElementInspector`
  (CompositionLocal), `Modifier.inspectId(id, onClick = {})` (Doppelklick via
  `combinedClickable`, inkl. Ripple; optionaler Einfach-Klick bleibt erhalten) für
  einzelne Elemente, `Modifier.inspectTap(id)` (ripple-frei via `detectTapGestures`,
  ohne Layout-Eingriff) für **Gruppen-Container**, `InspectSection(id)` als
  Sektions-Fallback und `ElementInspectorHost` mit dem gehosteten Popup.
  `HomeScreen` umschließt den gesamten Showcase mit dem Host.
- **ID-Benennung folgt exakt den Codes des HTML-Showcase** (`wafflehq-showcase-v2.html`):
  Schema `<Sektion><Variante-Buchstabe>.<Element-Nummer>`, z. B. `3a.4`. Die Codes
  werden im Showcase aus **Sektions-Nummer** (1…33), **Gruppe** (Buchstabe a, b, c…
  pro Variante/Spalte) und **Element-Nummer** (1, 2, 3…) gebildet.
  - **Gruppen** (wo der Showcase Gruppen-Badges hat) tragen den Code ohne Punkt,
    z. B. `3a`…`3g` (Hue-Rampen), `6a`…`6g` (Button-Panels), `8a`…`8d`
    (Icon-Button-Panels), `13a`–`13c`, `15a`/`15b`, `16a`–`16c`, `18a`/`18b`,
    `21a`/`21b`, `22a`–`22f`, `23a`/`24a`, `25a`–`33a`, `31a`/`31b`, `32a`–`32f`.
  - **Elemente** tragen den vollen Code, z. B. `1a.1`…`1a.15` (Typografie),
    `3a.1`…`3a.9` (Tones einer Rampe), `6a.1`…`6a.7` (Buttons), `19a.1`…`19a.16`
    (Icons), `25a.1`…`25a.6` (Listenzeilen).
  - Sektionen ohne Showcase-Gruppen nutzen die implizite Gruppe `a`
    (z. B. `2a.1`…`2a.5`, `4a.1`…`4a.6`, `5a.1`…`5a.7`).
  Die frühere fachliche Benennung (`primary.40`, `button.filled.primary`,
  `weight.light` …) wurde dadurch ersetzt. `verify-theme.sh` bleibt erfüllt, da die
  Codes keine Farb-Token-Namen enthalten.
- Geteilte Komponenten (`SettingsListContent`/`SettingsListRow`/`SettingsGroup` in
  `ui/components/SettingsUi.kt`, `DisplaySettingsContent`) erhielten **optionale**
  Code-Parameter (Default `null`); die echten Settings-Screens bleiben dadurch
  unverändert, nur die Showcase-Mockups (Sektion 23/24) setzen Codes.
- Sektions-Container behalten zusätzlich den Fallback-Code `"section.<slug>"`
  (z. B. `section.typography`) für Doppeltipp außerhalb konkreter Elemente.

## App-Header & Navigation

- Der **App-Header** (`uikit/components/AppHeader.kt`) ist die echte,
  persistente Kopfzeile aller Hauptseiten. Er delegiert intern an
  `uikit/navigation/AppTopNavBar` (1:1-Port aus periodicals
  `lib/navigation/.../AppTopNavBar`/`AppTopNavTab`) statt einer eigenen
  Pill-Implementierung, damit Header und Navigation-Drawer dauerhaft
  denselben periodical-Stil teilen. Höhe 72 dp + Status-Bar-Inset,
  Trennlinie an der Unterkante.
- Drei gleich breite Buttons (Icon über Beschriftung, zentriert):
  - **links Menü (Burger)** – öffnet den Navigation-Drawer.
  - **mittig Start** – führt zur Home/Showcase-Seite; aktiv auf Home.
  - **rechts Einstellungen (Zahnrad)** – führt zu den Einstellungen; aktiv dort.
  - **Aktiver Button**: 64×32 dp Pill hinter dem Icon (16 dp Radius,
    `AppNavColors.topBarSelectedPillBackground` = Primary-Container), statt
    einer Vollflächen-Highlight über die ganze Spalte — identisch zu
    periodicals Tab-Indikator. Farben kommen aus
    `uikit/navigation/AppNavColors.fromAppTheme()` (bereits als 1:1-Port aus
    periodical vorhanden), damit Header und Navigation-Shell dieselbe Palette
    teilen.
- `AppScaffold` kapselt Header + Inhalt und wird von Home, den Beispielseiten
  und den Einstellungen genutzt.
- **Navigation-Drawer** (`ui/components/AppDrawer.kt`): Einträge Start,
  Beispiel 1, Beispiel 2, Beispiel 3, Bibliotheken, Einstellungen. Delegiert an
  `uikit/navigation/AppSideNavDrawer` (1:1-Port aus periodicals
  `AppSideNavDrawer`) statt eines eigenen `NavigationDrawerItem`-Aufbaus:
  aktiver Eintrag hat Stadium-/Oval-Form (`CircleShape`), gefüllt mit
  Secondary-Container-Farbe und zusätzlich umrandet mit einem 1 dp blauen
  Rahmen (`AppNavColors.selectedPill` = Primary-Accent) — identisch zu
  periodicals Drawer-Highlight, nicht mehr die reine Flächen-Füllung ohne
  Rahmen.
- **Beispielseiten 1–3** (`ui/example/ExampleScreen.kt`): generische
  Lorem-ipsum-Seiten (Titel, Lead, drei Karten), nur über den Burger erreichbar.

## Settings-Screen

Die Einstellungen sind ein eigener Drill-in-Fluss (kein App-Header mit drei
Buttons), exakt wie Showcase-Sektion 23/24. Jede Seite hat eine eigene
Kopfzeile (`SettingsTopBar`): 56 dp hoch, Surface, Trennlinie an der Unterkante,
links ein Zurück-Pfeil (Chevron), daneben der Titel (Bold 18 sp).

- **Listenseite** (`SettingsScreen`, Route `settings`): erreichbar per
  Zahnrad-Button im App-Header oder über den Drawer (jeweils per Push, damit der
  Zurück-Pfeil funktioniert). Titel „Einstellungen“. Voll klickbare Zeilen
  (`SettingsListRow`) mit Chevron rechts, getrennt durch Trennlinien:
  - „Features“ (nur sichtbar, wenn mindestens eine Feature-Datei vorhanden) →
    öffnet die Feature-Liste.
  - „Anzeige“ mit Untertitel „Design, Schriftgrößen, Farben“ → öffnet die
    Anzeige-Unterseite.
- **Anzeige-Unterseite** (`DisplaySettingsScreen`, Route `settings_display`):
  Titel „Anzeige“. Gruppen (`SettingsGroup`) mit Mono-Label und leicht
  eingefärbtem Kästchen (Tint per `lerp` aus Surface + Rollen-Akzent, Border aus
  Outline + Akzent), getrennt durch Trennlinien:
  - „Allgemein“ (Primary-Tint): Design-**Dropdown** (`SettingsDropdownField`,
    eigene Pill-Trigger + `DropdownMenu`): Systemstandard / Hell / Dunkel.
    Auswahl wird sofort via DataStore persistiert und auf das gesamte App-Theme
    angewendet.
  - „Lorem Ipsum“ (Secondary-Tint) und „Consectetur“ (Tertiary-Tint):
    Beispiel-Gruppen mit Slider (`SettingsSliderControl`) und Switch
    (`SettingsSwitchRow`) als wiederverwendbare Muster; lokaler, nicht
    persistierter State.
- Die UI-Bausteine (`SettingsScaffold`, `SettingsTopBar`, `SettingsListContent`,
  `SettingsGroup`, `SettingsDropdownField`, `SettingsSliderControl`,
  `SettingsSwitchRow`) liegen in `ui/components/SettingsUi.kt`; die
  „Anzeige“-Inhalte als `DisplaySettingsContent` in `ui/settings/`. Dieselben
  Bausteine rendern die Showcase-Sektionen 23/24.

## Feature-Liste

- Listet alle Markdown-Dateien aus `features/*.md` im Projektstamm (werden
  beim Build per Gradle-Task in `assets/features/` einsynct).
- Pro Eintrag eine Checkbox (abhakbar; Status wird via DataStore als
  `Set<String>` persistiert).
- Checkbox "Ausgeblendete anzeigen" oben (Toggle persistiert). Wenn
  deaktiviert: abgehakte Einträge verschwinden aus der Liste.
- Klick auf einen Eintrag öffnet eine Detail-Seite mit dem vollen
  Markdown-Text als reiner Text.
- Vollständig dynamisch: keine Dateien → kein Settings-Eintrag, kein
  Listenrendering.

## Theme

- `ui/theme/Color.kt` – 7 Hue-Rampen (Sapphire/Aquamarine/Amethyst/Emerald/
  Citrine/Garnet/Graphite) je 9 Tones (10–90) gemäß `/wafflehq-design` und
  identisch zu den Hex-Werten in `wafflehq-showcase-v2.1.html`. Neutrale
  Surfaces für hell (`#F4F6FA`/`#DDE3EC`) und dunkel
  (`Abyss/Midnight/Dusk`). `OnSurfaceDark` ist `#B0BCC8` (Calm Display, nicht
  LED-weiß), `OutlineDark` ist `#586E88` (≥3:1 auf Abyss).
- `ui/theme/Theme.kt` – Material 3 Light/Dark Color-Scheme, dazu `AppColors`/
  `AppTokens` mit allen sieben Rollen (Primary…Neutral) über
  `LocalAppColors`/`LocalAppTokens` und `AppTheme.colors`/`AppTheme.tokens`.
- `ui/theme/Type.kt` – komplette M3-Skala mit WaffleHQ-Weights (700 für
  Display/Headline, 600 für Title/Label, 400 für Body). Hausschrift ist
  **Geist** (Sans + Mono), TTFs liegen in `res/font/`.

## Architektur

- Single-Activity (`MainActivity`) + Compose Navigation (`AppNavHost`).
- Hilt-DI mit Modul `AppModule` für DB + SettingsRepository.
- Room-DB `app.db` mit Placeholder-Entity (für echte Projekte ersetzen).
- DataStore Preferences (`settings`).

## i18n

- Deutsche Strings in `values-de/strings.xml`.
- Englische Fallback-Strings in `values/strings.xml`.
- Showcase-Strings sind bewusst generisch – keine Verweise mehr auf
  Periodical, CarFinder, ChordUtil oder TherapyFinder.
