# Showcase-Element-IDs (Inspector-IDs)

Diese Datei ist die **vollständige Registry** aller Inspector-IDs, die im
`:uikit`-Showcase (`uikit/src/main/java/com/wafflehq/uikit/showcase/Section*.kt`)
vergeben sind. Der Element-Inspektor (`ElementInspector.kt`,
`ElementInspectorHost(enabled = true)` in der Beispiel-App) zeigt beim
Doppeltipp auf ein Element genau diese ID in einem Dialog an (kopierbar) —
gedacht für Bugreports/Design-Reviews ("Button 6a.3 hat falsche Farbe").

Jede KI, die den `/base-project`-Skill nutzt, findet diese Datei im
Projektstamm von `/home/sklein/IdeaProjects/base-project` (siehe
`.claude/skills/base-project/SKILL.md` → Abschnitt „Showcase-ID-Mapping“, der
hierher verweist).

## Selbstpflege — MUSS nach jeder Showcase-Änderung aktualisiert werden

Diese Datei ist **selbstpflegend**: Jede KI, die eine `Section*.kt`-Datei im
Showcase-Verzeichnis ändert (ID hinzufügt, entfernt, umbenennt, neue
`SectionNN*.kt` anlegt), **muss diese Datei im selben Arbeitsschritt
nachziehen**. Kein Abschluss einer Showcase-Änderung ohne aktualisierte
`SHOWCASE-ELEMENT-IDS.md`.

Vorgehen zum Aktualisieren:

1. Alle Vorkommen von IDs im Showcase-Verzeichnis auflisten:
   ```
   grep -rn 'inspectId(\|inspectTap(\|inspectCode\s*=\|code\s*=' \
     uikit/src/main/java/com/wafflehq/uikit/showcase/*.kt
   ```
2. Für dynamisch gebaute IDs (String-Templates wie `"$groupCode.${i + 1}"`,
   `"32" + ('a' + index)`, `"29a.$memberIndex"`) den umgebenden Code lesen und
   **jede tatsächlich entstehende ID einzeln** auflisten — niemals nur das
   Muster dokumentieren.
3. Zweck-Beschreibung kurz halten (Komponente + Variante/Zustand + ggf.
   Label/Text), auf Deutsch, analog zu den bestehenden Einträgen unten.
   String-Ressourcen (`R.string.sc_*`) ggf. in
   `uikit/src/main/res/values/strings.xml` nachschlagen.
4. Abschnitt der betroffenen `SectionNN*.kt`-Datei unten ersetzen (nicht die
   ganze Datei neu schreiben).
5. Bei Widerspruch zwischen dieser Datei und dem tatsächlichen Code hat **der
   Code Vorrang** — diese Datei ist eine Ableitung des Codes, keine
   eigenständige Quelle. Im Zweifel neu aus dem Code ableiten statt raten.
6. Nach Abschluss: Zeilen-/ID-Anzahl grob mit `grep -c` gegenprüfen, damit
   keine ID beim Nachziehen verloren geht.

## ID-Schema

Format: `{Sektionsnummer}{Gruppe a/b/c/…}.{laufende Nummer}`, z. B. `13a.1`,
`22g.3`, `8a.5`.

- **Element-ID** (mit Punkt, z. B. `6a.3`): sitzt auf `Modifier.inspectId(id)`
  direkt am einzelnen UI-Element. Doppeltipp darauf zeigt genau diese ID.
- **Gruppen-ID** (ohne Punkt, z. B. `6a`, `8b`, `13c`, `18a`, `21a`, `32d`):
  sitzt auf `Modifier.inspectTap(id)` am umschließenden Panel/der Spalte/der
  Karte. Doppeltipp irgendwo in der Gruppe (aber nicht auf ein Kind-Element
  mit eigener Element-ID) zeigt diese Gruppen-ID.
- Manche Sektionen haben nur Element-IDs, manche nur Gruppen-IDs, manche
  beide (Gruppen-ID für die Karte + Element-IDs für die Kinder).
- Die Buchstabengruppe (a, b, c, …) steht meist für eine Rolle/Variante
  (Primary/Secondary/…) oder eine thematische Untergruppe innerhalb der
  Sektion; die laufende Nummer zählt die Kinder der Gruppe durch.

Mechanismus-Quelle: `uikit/src/main/java/com/wafflehq/uikit/showcase/ElementInspector.kt`
(`inspectId`, `inspectTap`, `InspectSection`, `ElementInspectorHost`).

---

## Section01Typography.kt

- 1a.1 — Typo-Zeile Display L (Beispieltext „Display“)
- 1a.2 — Typo-Zeile Display M
- 1a.3 — Typo-Zeile Display S
- 1a.4 — Typo-Zeile Headline L (Beispieltext „Headline“)
- 1a.5 — Typo-Zeile Headline M
- 1a.6 — Typo-Zeile Headline S
- 1a.7 — Typo-Zeile Title L (Beispieltext „Title“)
- 1a.8 — Typo-Zeile Title M
- 1a.9 — Typo-Zeile Title S
- 1a.10 — Typo-Zeile Body L (Beispieltext „Body“)
- 1a.11 — Typo-Zeile Body M
- 1a.12 — Typo-Zeile Body S
- 1a.13 — Typo-Zeile Label L (Beispieltext „Label“)
- 1a.14 — Typo-Zeile Label M
- 1a.15 — Typo-Zeile Label S

## Section02Weights.kt

- 2a.1 — Schriftschnitt Light · 300, Beispielzeile
- 2a.2 — Schriftschnitt Regular · 400, Beispielzeile
- 2a.3 — Schriftschnitt Medium · 500, Beispielzeile
- 2a.4 — Schriftschnitt SemiBold · 600, Beispielzeile
- 2a.5 — Schriftschnitt Bold · 700, Beispielzeile

## Section03Ramps.kt

Gruppen-ID = ganze Ramp-Karte (Doppeltipp irgendwo auf der Karte); Element-IDs
= einzelne Farbkacheln, Tonstufen 10–90 in fester Reihenfolge.

- 3a — Ramp-Karte Sapphire/Primary
- 3a.1 — Sapphire Tonstufe 10
- 3a.2 — Sapphire Tonstufe 20
- 3a.3 — Sapphire Tonstufe 30
- 3a.4 — Sapphire Tonstufe 40
- 3a.5 — Sapphire Tonstufe 50
- 3a.6 — Sapphire Tonstufe 60
- 3a.7 — Sapphire Tonstufe 70
- 3a.8 — Sapphire Tonstufe 80
- 3a.9 — Sapphire Tonstufe 90
- 3b — Ramp-Karte Aquamarine/Secondary
- 3b.1 — Aquamarine Tonstufe 10
- 3b.2 — Aquamarine Tonstufe 20
- 3b.3 — Aquamarine Tonstufe 30
- 3b.4 — Aquamarine Tonstufe 40
- 3b.5 — Aquamarine Tonstufe 50
- 3b.6 — Aquamarine Tonstufe 60
- 3b.7 — Aquamarine Tonstufe 70
- 3b.8 — Aquamarine Tonstufe 80
- 3b.9 — Aquamarine Tonstufe 90
- 3c — Ramp-Karte Amethyst/Tertiary
- 3c.1 — Amethyst Tonstufe 10
- 3c.2 — Amethyst Tonstufe 20
- 3c.3 — Amethyst Tonstufe 30
- 3c.4 — Amethyst Tonstufe 40
- 3c.5 — Amethyst Tonstufe 50
- 3c.6 — Amethyst Tonstufe 60
- 3c.7 — Amethyst Tonstufe 70
- 3c.8 — Amethyst Tonstufe 80
- 3c.9 — Amethyst Tonstufe 90
- 3d — Ramp-Karte Emerald/Success
- 3d.1 — Emerald Tonstufe 10
- 3d.2 — Emerald Tonstufe 20
- 3d.3 — Emerald Tonstufe 30
- 3d.4 — Emerald Tonstufe 40
- 3d.5 — Emerald Tonstufe 50
- 3d.6 — Emerald Tonstufe 60
- 3d.7 — Emerald Tonstufe 70
- 3d.8 — Emerald Tonstufe 80
- 3d.9 — Emerald Tonstufe 90
- 3e — Ramp-Karte Citrine/Warning
- 3e.1 — Citrine Tonstufe 10
- 3e.2 — Citrine Tonstufe 20
- 3e.3 — Citrine Tonstufe 30
- 3e.4 — Citrine Tonstufe 40
- 3e.5 — Citrine Tonstufe 50
- 3e.6 — Citrine Tonstufe 60
- 3e.7 — Citrine Tonstufe 70
- 3e.8 — Citrine Tonstufe 80
- 3e.9 — Citrine Tonstufe 90
- 3f — Ramp-Karte Garnet/Error
- 3f.1 — Garnet Tonstufe 10
- 3f.2 — Garnet Tonstufe 20
- 3f.3 — Garnet Tonstufe 30
- 3f.4 — Garnet Tonstufe 40
- 3f.5 — Garnet Tonstufe 50
- 3f.6 — Garnet Tonstufe 60
- 3f.7 — Garnet Tonstufe 70
- 3f.8 — Garnet Tonstufe 80
- 3f.9 — Garnet Tonstufe 90
- 3g — Ramp-Karte Graphite/Neutral
- 3g.1 — Graphite Tonstufe 10
- 3g.2 — Graphite Tonstufe 20
- 3g.3 — Graphite Tonstufe 30
- 3g.4 — Graphite Tonstufe 40
- 3g.5 — Graphite Tonstufe 50
- 3g.6 — Graphite Tonstufe 60
- 3g.7 — Graphite Tonstufe 70
- 3g.8 — Graphite Tonstufe 80
- 3g.9 — Graphite Tonstufe 90

## Section04Surfaces.kt

- 4a.1 — Farbkachel Background (mit Outline-Rand)
- 4a.2 — Farbkachel Surface (mit Outline-Rand)
- 4a.3 — Farbkachel Surface variant (mit Outline-Rand)
- 4a.4 — Farbkachel Outline
- 4a.5 — Farbkachel On-surface
- 4a.6 — Farbkachel On-surface variant

## Section05Roles.kt

- 5a.1 — Rollen-Karte Primary · Sapphire, Kontrast AAA
- 5a.2 — Rollen-Karte Secondary · Aquamarine, Kontrast AAA
- 5a.3 — Rollen-Karte Tertiary · Amethyst, Kontrast AAA
- 5a.4 — Rollen-Karte Success · Emerald, Kontrast AAA
- 5a.5 — Rollen-Karte Warning · Citrine, Kontrast AA (nicht AAA)
- 5a.6 — Rollen-Karte Error · Garnet, Kontrast AAA
- 5a.7 — Rollen-Karte Neutral · Graphite, Kontrast AAA

## Section06Buttons.kt

Gruppen-ID = ganzes Button-Panel einer Rolle; Element-IDs = einzelne Buttons.

- 6a — Button-Panel Primary
- 6a.1 — Button „Save“, Filled, Primary
- 6a.2 — Button „Edit“, Tonal, Primary
- 6a.3 — Button „Share“, Elevated, Primary
- 6a.4 — Button „Filter“, Outlined, Primary
- 6a.5 — Button „More“, Text, Primary
- 6a.6 — Button „Disabled“, Filled, deaktiviert, Primary
- 6a.7 — Button „Disabled“, Outlined, deaktiviert, Primary
- 6b — Button-Panel Secondary
- 6b.1 — Button „Reminder“, Filled, Secondary
- 6b.2 — Button „Mark“, Tonal, Secondary
- 6b.3 — Button „Note“, Elevated, Secondary
- 6b.4 — Button „Filter“, Outlined, Secondary
- 6b.5 — Button „More“, Text, Secondary
- 6b.6 — Button „Disabled“, Filled, deaktiviert, Secondary
- 6c — Button-Panel Tertiary
- 6c.1 — Button „Forecast“, Filled, Tertiary
- 6c.2 — Button „Cycle“, Tonal, Tertiary
- 6c.3 — Button „Stats“, Elevated, Tertiary
- 6c.4 — Button „Options“, Outlined, Tertiary
- 6c.5 — Button „More“, Text, Tertiary
- 6d — Button-Panel Success
- 6d.1 — Button „Confirm“, Filled, Success
- 6d.2 — Button „Successful“, Tonal, Success
- 6d.3 — Button „Callable“, Elevated, Success
- 6d.4 — Button „Active“, Outlined, Success
- 6d.5 — Button „Details“, Text, Success
- 6e — Button-Panel Warning
- 6e.1 — Button „Battery note“, Filled, Warning
- 6e.2 — Button „Holiday“, Tonal, Warning
- 6e.3 — Button „Check“, Elevated, Warning
- 6e.4 — Button „Verify“, Outlined, Warning
- 6e.5 — Button „More“, Text, Warning
- 6f — Button-Panel Error
- 6f.1 — Button „Delete“, Filled, Error
- 6f.2 — Button „Error“, Tonal, Error
- 6f.3 — Button „Discard“, Elevated, Error
- 6f.4 — Button „Stop“, Outlined, Error
- 6f.5 — Button „More“, Text, Error
- 6g — Button-Panel Neutral
- 6g.1 — Button „Standard“, Filled, Neutral
- 6g.2 — Button „Metadata“, Tonal, Neutral
- 6g.3 — Button „Archive“, Elevated, Neutral
- 6g.4 — Button „Secondary“, Outlined, Neutral
- 6g.5 — Button „More“, Text, Neutral

## Section07Fab.kt

- 7a.1 — FAB klein, 40 dp, Primary
- 7a.2 — FAB Standard, 56 dp, Primary
- 7a.3 — FAB groß, 96 dp, Primary
- 7a.4 — Extended FAB „Add event“, Primary

## Section08IconButtons.kt

Gruppen-ID = ganzes Icon-Button-Panel/-Spalte; Element-IDs = einzelne Icon-Buttons.

- 8a — Icon-Button-Panel Primary
- 8a.1 — Icon-Button Edit, Standard-Variante, Primary
- 8a.2 — Icon-Button Check, Filled-Variante, Primary
- 8a.3 — Icon-Button Search, Tonal-Variante, Primary
- 8a.4 — Icon-Button Share, Outlined-Variante, Primary
- 8a.5 — Icon-Button Edit, Standard, deaktiviert
- 8a.6 — Icon-Button Check, Filled, deaktiviert
- 8a.7 — Icon-Button Search, Tonal, deaktiviert
- 8b — Icon-Button-Spalte Error
- 8b.1 — Icon-Button Delete, Filled, Error
- 8c — Icon-Button-Spalte Success
- 8c.1 — Icon-Button Favorite, Tonal, Success
- 8d — Icon-Button-Spalte Tertiary
- 8d.1 — Icon-Button Info, Outlined, Tertiary

## Section09Chips.kt

- 9a.1 — Assist-Chip „Show tip“ mit Info-Icon
- 9a.2 — Outline-Chip „Add to list“
- 9a.3 — Container-Chip „✓ Callable“, Success
- 9a.4 — Container-Chip „Urgent“, Error
- 9a.5 — Filter-Chip „All“, ausgewählt (Standard)
- 9a.6 — Filter-Chip „Callable today“
- 9a.7 — Filter-Chip „With capacity“
- 9a.8 — Filter-Chip „Nearby“
- 9a.9 — Input-Chip „Anna M.“ (AM), ausgewählt, entfernbar, Primary
- 9a.10 — Input-Chip „Jonas B.“ (JB), nicht ausgewählt, nicht entfernbar, Primary
- 9a.11 — Input-Chip „Sara F.“ (SF), ausgewählt, entfernbar, Error

## Section10TextFields.kt

- 10a.1 — Outlined-Textfeld „Name“, leer mit Placeholder
- 10a.2 — Outlined-Textfeld „Search“ mit Such-Icon
- 10a.3 — Outlined-Textfeld „E-mail“, Fehlerzustand, Wert „anna@“
- 10a.4 — Filled-Textfeld „Note“, mehrzeilig
- 10a.5 — Outlined-Textfeld „Phone“, deaktiviert/gesperrt

## Section11Cards.kt

- 11a.1 — Card-Variante Filled
- 11a.2 — Card-Variante Elevated
- 11a.3 — Card-Variante Outlined

## Section12List.kt

- 12a.1 — Listenzeile einzeilig „One line“, Trailing „14:08“
- 12a.2 — Listenzeile zweizeilig „Anna Müller“ mit E-Mail-Sub, Trailing „Today“
- 12a.3 — Listenzeile dreizeilig „Bismarckstraße car park“ mit Sub + Zusatzzeile, Trailing Distanz

## Section13Selection.kt

Gruppen-ID = ganze Spalte (Checkbox/Switch/Radio); Element-IDs = einzelne Controls.

- 13a — Checkbox-Spalte
- 13a.1 — Checkbox „Enabled“, aktiviert
- 13a.2 — Checkbox „Disabled“ (Label), nicht aktiviert
- 13a.3 — Checkbox „Locked“, aktiviert aber gesperrt (enabled=false)
- 13b — Switch-Spalte
- 13b.1 — Switch „Light mode“, an
- 13b.2 — Switch „Push reminder“, aus
- 13b.3 — Switch „Locked“, aus und gesperrt
- 13c — Radio-Spalte
- 13c.1 — Radio „System default“, ausgewählt
- 13c.2 — Radio „Light“
- 13c.3 — Radio „Dark“

## Section14Segmented.kt

- 14a.1 — Segmented Button „Day“, ausgewählt (Standard)
- 14a.2 — Segmented Button „Week“
- 14a.3 — Segmented Button „Month“
- 14a.4 — Segmented Button „Year“

## Section15SliderProgress.kt

Gruppen-ID = ganze Spalte (Slider/Progress); Element-IDs = einzelne Controls.

- 15a — Slider-Spalte
- 15a.1 — Slider kontinuierlich, 35 %
- 15a.2 — Slider gestuft, 9 Schritte, 7/10
- 15a.3 — Slider deaktiviert/gesperrt, 60 %
- 15b — Progress-Spalte
- 15b.1 — Lineare Fortschrittsanzeige, determinate 65 %
- 15b.2 — Lineare Fortschrittsanzeige, indeterminate
- 15b.3 — Kreisförmige Fortschrittsanzeige, indeterminate

## Section16Badges.kt

Gruppen-ID = ganze WrapRow (Icon-Badges/Status-Pills/Text-Pills); Element-IDs
= einzelne Badges/Pills.

- 16a — Icon-Badge-Gruppe
- 16a.1 — Icon-Badge Notifications mit Zahl „12“
- 16a.2 — Icon-Badge Favorite mit Zahl „3“
- 16a.3 — Icon-Badge Search mit Punkt-Badge (ohne Zahl)
- 16b — Status-Pill-Gruppe
- 16b.1 — Status-Pill Primary (mit Punkt)
- 16b.2 — Status-Pill Secondary
- 16b.3 — Status-Pill Tertiary
- 16b.4 — Status-Pill Success
- 16b.5 — Status-Pill Warning
- 16b.6 — Status-Pill Error
- 16b.7 — Status-Pill Neutral
- 16c — Text-Pill-Gruppe
- 16c.1 — Status-Pill „✓ Saved“, Success, ohne Punkt
- 16c.2 — Status-Pill „✗ Discarded“, Error, ohne Punkt

## Section17Banners.kt

- 17a.1 — Banner Primary „New feature available“, schließbar
- 17a.2 — Banner Success „Backup saved“, schließbar
- 17a.3 — Banner Warning „Battery optimization active“, schließbar
- 17a.4 — Banner Error „Drive token expired“, schließbar
- 17a.5 — Banner Neutral „No entries yet“, schließbar
- 17a.6 — Banner Secondary „Syncing“, schließbar
- 17a.7 — Banner Tertiary „Tip: add a widget“, schließbar
- 17a.8 — Banner Primary „Update available“ mit Aktion „Load now“
- 17a.9 — Banner Error gefüllt „Storage almost full“ mit Aktion „Manage“

## Section18SnackbarDialog.kt

Gruppen-IDs = Snackbar-Gruppe / Dialog-Vorschau-Gruppe / Live-Dialog-Gruppe.

- 18a — Snackbar-Gruppe
- 18a.1 — Snackbar „Entry deleted.“ mit Undo-Aktion
- 18a.2 — Snackbar „Location saved · ±12 m“, ohne Aktion
- 18b — Dialog-Vorschau-Gruppe
- 18b.1 — Statische Dialog-Vorschau „Delete entry?“ (Löschbestätigung)
- 18c — Live-Dialog-Gruppe
- 18c.1 — Trigger-Button „Open dialog“ (öffnet echten AppDialog)
- 18c.2 — Dialog-Abbrechen-Button („Cancel“)
- 18c.3 — Dialog-Bestätigen-Button („Delete“, Error)

## Section19Icons.kt

- 19a.1 — Icon-Kachel Home
- 19a.2 — Icon-Kachel CalendarMonth
- 19a.3 — Icon-Kachel Schedule
- 19a.4 — Icon-Kachel LocationOn
- 19a.5 — Icon-Kachel Notifications
- 19a.6 — Icon-Kachel Search
- 19a.7 — Icon-Kachel LocalShipping
- 19a.8 — Icon-Kachel ChatBubbleOutline
- 19a.9 — Icon-Kachel Check
- 19a.10 — Icon-Kachel Close
- 19a.11 — Icon-Kachel DeleteOutline
- 19a.12 — Icon-Kachel Edit
- 19a.13 — Icon-Kachel CheckBox
- 19a.14 — Icon-Kachel Settings
- 19a.15 — Icon-Kachel ChevronLeft
- 19a.16 — Icon-Kachel Favorite (gefüllt)

## Section20Dividers.kt

- 20a.1 — Horizontaler Divider, dünn (1 dp)
- 20a.2 — Horizontaler Divider, stark (2 dp)
- 20a.3 — Vertikaler Divider zwischen „Left“ und „Center“
- 20a.4 — Vertikaler Divider zwischen „Center“ und „Right“

## Section21Spacing.kt

Gruppen-ID = ganze Spalte (Spacing-Werte/Radii); Element-IDs = einzelne Zeilen.

- 21a — Spacing-Spalte
- 21a.1 — Spacing-Wert xs, 4 px
- 21a.2 — Spacing-Wert sm, 8 px
- 21a.3 — Spacing-Wert md, 12 px
- 21a.4 — Spacing-Wert lg, 16 px
- 21a.5 — Spacing-Wert xl, 24 px
- 21a.6 — Spacing-Wert xxl, 32 px
- 21b — Radii-Spalte
- 21b.1 — Radius xs, 4 px, Outlined-Textfeld
- 21b.2 — Radius s, 8 px, Snackbar/kleine Tags
- 21b.3 — Radius m, 12 px, Card/List-Tile
- 21b.4 — Radius l, 16 px, FAB/Hero-Card
- 21b.5 — Radius xl, 28 px, Dialog/Bottom-Sheet
- 21b.6 — Radius pill, Buttons/Chips

## Section22AppHeader.kt

Telefon-Mocks (Gruppen-IDs 22a–22f) demonstrieren `AppHeader`-Zustände;
22g ist die echte `AppSideNavDrawer`-Komponente mit Nav-Items als Element-IDs.

- 22a — Telefon-Mock Variante A (2-Page-App)
- 22a.1 — Header-Leiste Variante A (List/Home aktiv/Settings)
- 22b — Telefon-Mock Variante B (Multi-Page-App)
- 22b.1 — Header-Leiste Variante B (Burger/Home aktiv/Settings)
- 22c — Telefon-Mock Standardzustand (flach)
- 22c.1 — Header-Leiste Standardzustand, kein Tab aktiv
- 22d — Telefon-Mock elevated (gescrollt)
- 22d.1 — Header-Leiste mit Schatten (elevated)
- 22e — Telefon-Mock Badge-Zustand
- 22e.1 — Header-Leiste mit Zahlen-Badge (Menu) und Punkt-Badge (Settings)
- 22f — Telefon-Mock Drawer geöffnet
- 22f.1 — Header-Leiste mit geöffnetem Navigations-Drawer-Overlay
- 22g — Side-Menu-Mock (echte AppSideNavDrawer-Komponente)
- 22g.1 — Drawer-Nav-Item „Home“, ausgewählt
- 22g.2 — Drawer-Nav-Item „Calendar“
- 22g.3 — Drawer-Nav-Item „History“
- 22g.4 — Drawer-Nav-Item „Settings“

## Section23SettingsList.kt

- 23a — Settings-Listen-Mock
- 23a.1 — Settings-Listenzeile „Features“
- 23a.2 — Settings-Listenzeile „Display“ mit Untertitel

## Section24SettingsDetail.kt

- 24a — Settings-Detail-Mock
- 24a.1 — Settings-Gruppe „General“ mit Theme-Dropdown (System/Light/Dark)
- 24a.2 — Settings-Gruppe „Lorem ipsum“ mit Font-Size-Slider + Switch „Dolor sit amet“
- 24a.3 — Settings-Gruppe „Consectetur“ mit Contrast-Slider + Switch „Adipiscing elit“

## Section25FilterList.kt

- 25a — Filterbare-Kontaktliste-Karte
- 25a.1 — Kontaktzeile Anna Meyer, Project lead
- 25a.2 — Kontaktzeile Jonas Brandt, Development
- 25a.3 — Kontaktzeile Clara Voss, Design · Agency North
- 25a.4 — Kontaktzeile Leon Faber, Backend
- 25a.5 — Kontaktzeile Mara Sommer, Purchasing · Supplier South
- 25a.6 — Kontaktzeile Timo Reuter, QA & test

## Section26DndList.kt

- 26a — Drag&Drop-Listen-Karte
- 26a.1 — Aufgabe „Finalize onboarding flow“, Rang 1, Today
- 26a.2 — Aufgabe „Review API contract“, Rang 2, Tue
- 26a.3 — Aufgabe „Export design tokens“, Rang 3, Wed
- 26a.4 — Aufgabe „Write release notes“, Rang 4, Fri
- 26a.5 — Aufgabe „Prioritize backlog“, Rang 5, Next week

## Section27DeleteList.kt

- 27a — Auswahl&Löschen-Listen-Karte
- 27a.1 — Dateizeile „Quarterly-report.pdf“, PDF
- 27a.2 — Dateizeile „Moodboard.fig“, FIG
- 27a.3 — Dateizeile „Notes.md“, MD
- 27a.4 — Dateizeile „Logo-export.zip“, ZIP

## Section28PlainList.kt

- 28a — Plain-Liste „Chapters“-Karte
- 28a.1 — Plain-Zeile „Introduction“, p. 1
- 28a.2 — Plain-Zeile „Foundations & tokens“, p. 4, hervorgehoben (primary container)
- 28a.3 — Plain-Zeile „Components“, p. 12
- 28a.4 — Plain-Zeile „Patterns & layout“, p. 28
- 28a.5 — Plain-Zeile „Appendix“, p. 40
- 28b — Plain-Liste „Status“-Karte
- 28b.1 — Plain-Zeile „In progress“, 3, hervorgehoben + Marker-Punkt
- 28b.2 — Plain-Zeile „Awaiting review“, 5, mit Marker-Punkt
- 28b.3 — Plain-Zeile „Done“, 11, mit Marker-Punkt
- 28b.4 — Plain-Zeile „Archived“, 42, mit Marker-Punkt

## Section29GroupedList.kt

- 29a — Gruppierte-Liste-Karte
- 29a.1 — Mitglied Anna Meyer, Project lead, Gruppe A, favorisiert (★)
- 29a.2 — Mitglied Ali Koç, Marketing, Gruppe A
- 29a.3 — Mitglied Clara Voss, Design, Gruppe C
- 29a.4 — Mitglied Jonas Brandt, Development, Gruppe J
- 29a.5 — Mitglied Leon Faber, Backend, Gruppe L, favorisiert (★)
- 29a.6 — Mitglied Timo Reuter, QA & test, Gruppe T

## Section30AccordionList.kt

- 30a — Accordion/FAQ-Karte
- 30a.1 — FAQ-Eintrag „How do I export the theme?“, initial geöffnet
- 30a.2 — FAQ-Eintrag „Which color contrasts are met?“
- 30a.3 — FAQ-Eintrag „Is there a dark theme?“

## Section31ControlList.kt

- 31a — Notifications-Liste mit Switch
- 31a.1 — Zeile „Push notifications“, Switch an
- 31a.2 — Zeile „Email summary“, Switch aus
- 31a.3 — Zeile „Reminders“, Switch an
- 31b — Inbox-Liste mit Checkbox/Badge
- 31b.1 — Checkbox-Zeile „Check design tokens“, erledigt
- 31b.2 — Checkbox-Zeile „Prepare release“, offen
- 31b.3 — Badge-Zeile „Project updates“, Zähler „7“
- 31b.4 — Badge-Zeile „Team chat“, Zähler „24“

## Section32ContainerBoxes.kt

Gruppen-ID = ganze Farbrollen-Reihe; Element-IDs = die 6 Tint-Boxen je Reihe
(Outline 0 % → Very light 6 % → Light 12 % → Medium 22 % → Container 100 % →
Solid, Volltonfarbe).

- 32a — Container-Box-Reihe Primary/Sapphire
- 32a.1 — Box Outline, 0 %
- 32a.2 — Box Very light, 6 %
- 32a.3 — Box Light, 12 %
- 32a.4 — Box Medium, 22 %
- 32a.5 — Box Container, 100 % (Container-Fill)
- 32a.6 — Box Solid, full (Volltonfarbe)
- 32b — Container-Box-Reihe Secondary/Aquamarine
- 32b.1 — Box Outline, 0 %
- 32b.2 — Box Very light, 6 %
- 32b.3 — Box Light, 12 %
- 32b.4 — Box Medium, 22 %
- 32b.5 — Box Container, 100 %
- 32b.6 — Box Solid, full
- 32c — Container-Box-Reihe Tertiary/Amethyst
- 32c.1 — Box Outline, 0 %
- 32c.2 — Box Very light, 6 %
- 32c.3 — Box Light, 12 %
- 32c.4 — Box Medium, 22 %
- 32c.5 — Box Container, 100 %
- 32c.6 — Box Solid, full
- 32d — Container-Box-Reihe Success/Emerald
- 32d.1 — Box Outline, 0 %
- 32d.2 — Box Very light, 6 %
- 32d.3 — Box Light, 12 %
- 32d.4 — Box Medium, 22 %
- 32d.5 — Box Container, 100 %
- 32d.6 — Box Solid, full
- 32e — Container-Box-Reihe Warning/Citrine
- 32e.1 — Box Outline, 0 %
- 32e.2 — Box Very light, 6 %
- 32e.3 — Box Light, 12 %
- 32e.4 — Box Medium, 22 %
- 32e.5 — Box Container, 100 %
- 32e.6 — Box Solid, full
- 32f — Container-Box-Reihe Error/Garnet
- 32f.1 — Box Outline, 0 %
- 32f.2 — Box Very light, 6 %
- 32f.3 — Box Light, 12 %
- 32f.4 — Box Medium, 22 %
- 32f.5 — Box Container, 100 %
- 32f.6 — Box Solid, full

## Section33ComboList.kt

- 33a — Kombinierte Liste (Suche/Filter/Sortierung/Auswahl/Löschen)-Karte
- 33a.1 — Zeile Anna Meyer, Project lead
- 33a.2 — Zeile Jonas Brandt, Development
- 33a.3 — Zeile Clara Voss, Design · Agency North
- 33a.4 — Zeile Leon Faber, Backend
- 33a.5 — Zeile Mara Sommer, Purchasing · Supplier South
- 33a.6 — Zeile Timo Reuter, QA & test
