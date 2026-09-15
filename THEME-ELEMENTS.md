# Theme-Elemente Referenz

Vollständige Auflistung aller Theme-Elemente mit IDs, Funktionsbeschreibung und Farbreferenzen.

---

## Farbrampen (Color Ramps)

| ID | Name | Beschreibung | Tones |
|---|---|---|---|
| ramp.sapphire | Sapphire | Primary-Ramp für Primäraktionen und Fokuszustände | 10–90 (sapphire-10 bis sapphire-90) |
| ramp.aquamarine | Aquamarine | Secondary-Ramp für gleichrangige Alternative | 10–90 (aquamarine-10 bis aquamarine-90) |
| ramp.amethyst | Amethyst | Tertiary-Ramp für dritte Farbgruppe | 10–90 (amethyst-10 bis amethyst-90) |
| ramp.emerald | Emerald | Success-Ramp für erfolgreich / bestätigt | 10–90 (emerald-10 bis emerald-90) |
| ramp.citrine | Citrine | Warning-Ramp für Warnung / Vorsicht | 10–90 (citrine-10 bis citrine-90) |
| ramp.garnet | Garnet | Error-Ramp für Fehler / Löschen | 10–90 (garnet-10 bis garnet-90) |
| ramp.graphite | Graphite | Neutral-Ramp für Grautöne / Sekundärtext | 10–90 (graphite-10 bis graphite-90) |

---

## Rolle-Farben (Role Colors)

Für jede Rolle (Primary, Secondary, Tertiary, Success, Warning, Error, Neutral):

| ID | Komponente | Beschreibung | Light | Dark |
|---|---|---|---|---|
| role.{role}.accent | Accent | Hauptfarbe der Rolle | ramp-40 | ramp-80 |
| role.{role}.onAccent | On Accent | Text/Icon auf Accent | white | ramp-20 |
| role.{role}.container | Container | Hintergrund für Tonal-Varianten | ramp-90 | ramp-30 |
| role.{role}.onContainer | On Container | Text/Icon auf Container | ramp-10 | ramp-90 |
| role.{role}.tonalBorder | Tonal Border | Rahmen für Tonal-Elemente | ramp-30 | ramp-70 |

**Beispiele:**
- `primary.accent` = sapphire-40 (light) / sapphire-80 (dark)
- `error.container` = garnet-90 (light) / garnet-30 (dark)

---

## Oberflächen-Tokens (Surface Tokens)

| ID | Komponente | Beschreibung | Light | Dark |
|---|---|---|---|---|
| surface.background | Background | App-Hintergrund | #F4F6FA | #080E18 |
| surface.onBackground | On Background | Text auf Background | #191C22 | #B0BCC8 |
| surface.surface | Surface | Kartenhintergrund | #FFFFFF | #0E1825 |
| surface.onSurface | On Surface | Text auf Surface | #191C22 | #B0BCC8 |
| surface.surfaceVariant | Surface Variant | Sekundärer Hintergrund | #DDE3EC | #1A2535 |
| surface.onSurfaceVariant | On Surface Variant | Sekundärtext | #424850 | #7588A0 |
| surface.surface3 | Surface 3 | Tertiärer Hintergrund | #CBD2DE | #243047 |
| surface.outline | Outline | Rahmen / Divider | #72788A | #586E88 |

---

## Button-Tokens

| ID | Rolle | Variante | Beschreibung | Background | Content |
|---|---|---|---|---|---|
| button.{role}.filledBg | Alle | Filled | Durchgehend farbig | {role}.accent | {role}.onAccent |
| button.{role}.filledContent | Alle | Filled | Schrift / Icon | – | {role}.onAccent |
| button.{role}.tonalBg | Alle | Tonal | Gedimmt auf Ramp-90 | {role}.container | {role}.accent |
| button.{role}.tonalContent | Alle | Tonal | Schrift / Icon | – | {role}.accent |
| button.{role}.tonalBorder | Alle | Tonal | Rahmen | – | {role}.tonalBorder |
| button.{role}.elevatedBg | Alle | Elevated | Mit Schatten | {role}.container | {role}.accent |
| button.{role}.elevatedContent | Alle | Elevated | Schrift / Icon | – | {role}.accent |
| button.{role}.outlinedBorder | Alle | Outlined | Rahmen | – | {role}.accent |
| button.{role}.outlinedContent | Alle | Outlined | Schrift / Icon | – | {role}.accent |
| button.{role}.textContent | Alle | Text | Nur Text | – | {role}.accent |
| button.disabledBg | Alle | Alle | Deaktiviert | surfaceVariant | – |
| button.disabledContent | Alle | Alle | Deaktiviert | – | onSurfaceVariant |

---

## Icon-Button-Tokens

| ID | Rolle | Variante | Beschreibung | Background | Content |
|---|---|---|---|---|---|
| iconButton.{role}.standardContent | Alle | Standard | Nur Icon | – | {role}.accent |
| iconButton.{role}.filledBg | Alle | Filled | Durchgehend farbig | {role}.accent | – |
| iconButton.{role}.filledContent | Alle | Filled | Icon | – | {role}.onAccent |
| iconButton.{role}.tonalBg | Alle | Tonal | Gedimmt | {role}.container | – |
| iconButton.{role}.tonalContent | Alle | Tonal | Icon | – | {role}.accent |
| iconButton.{role}.outlinedBorder | Alle | Outlined | Rahmen | – | {role}.accent |
| iconButton.{role}.outlinedContent | Alle | Outlined | Icon | – | {role}.accent |
| iconButton.disabledBg | Alle | Alle | Deaktiviert | surfaceVariant | – |
| iconButton.disabledContent | Alle | Alle | Deaktiviert | – | onSurfaceVariant |

---

## Chip-Tokens

| ID | Rolle | Typ | Beschreibung | Background | Border | Content |
|---|---|---|---|---|---|---|
| chip.{role}.assistBg | Alle | Assist | Assistent-Vorschlag | {role}.container | – | {role}.accent |
| chip.{role}.assistBorder | Alle | Assist | Rahmen | – | outline | – |
| chip.{role}.assistContent | Alle | Assist | Text | – | – | {role}.accent |
| chip.{role}.suggestionBg | Alle | Suggestion | Varianten-Vorschlag | {role}.container | – | {role}.accent |
| chip.{role}.filterSelectedBg | Alle | Filter | Ausgewählt | {role}.accent | – | {role}.onAccent |
| chip.{role}.filterUnselectedBg | Alle | Filter | Nicht ausgewählt | {role}.container | – | {role}.accent |
| chip.{role}.filterUnselectedBorder | Alle | Filter | Rahmen | – | outline | – |
| chip.{role}.inputSelectedBg | Alle | Input | Ausgewählt (freie Eingabe) | {role}.accent | – | {role}.onAccent |
| chip.{role}.inputUnselectedBg | Alle | Input | Nicht ausgewählt | {role}.container | – | {role}.accent |
| chip.{role}.inputUnselectedBorder | Alle | Input | Rahmen | – | outline | – |

---

## Card-Tokens

### Base
| ID | Beschreibung | Farbe |
|---|---|---|
| card.base.filledBg | Gefüllte Karte (Hintergrund) | surface |
| card.base.elevatedBg | Karte mit Schatten (Hintergrund) | surface |
| card.base.outlinedBg | Outlined Karte (Hintergrund) | surface |
| card.base.outlinedBorder | Outlined Karte (Rahmen) | outline |

### Pro Rolle
| ID | Variante | Beschreibung | Background | Content |
|---|---|---|---|---|
| card.{role}.filledBg | Filled | Durchgehend farbig | {role}.container | {role}.onContainer |
| card.{role}.filledContent | Filled | Schrift / Icon | – | {role}.onContainer |
| card.{role}.elevatedBg | Elevated | Mit Schatten | {role}.container | {role}.onContainer |
| card.{role}.elevatedContent | Elevated | Schrift / Icon | – | {role}.onContainer |

---

## TextField-Tokens

| ID | Rolle | Beschreibung | Farbe |
|---|---|---|---|
| textField.{role}.background | Alle | Hintergrund | surface |
| textField.{role}.content | Alle | Text-Farbe | onSurface |
| textField.{role}.labelFocused | Alle | Label (fokussiert) | {role}.accent |
| textField.{role}.labelUnfocused | Alle | Label (unfokussiert) | onSurfaceVariant |
| textField.{role}.borderFocused | Alle | Rahmen (fokussiert) | {role}.accent |
| textField.{role}.borderUnfocused | Alle | Rahmen (unfokussiert) | outline |
| textField.{role}.errorBorder | Alle | Fehler-Rahmen | error.accent |
| textField.{role}.errorLabel | Alle | Fehler-Label | error.accent |
| textField.disabledBg | Alle | Deaktiviert (Hintergrund) | surfaceVariant |
| textField.disabledContent | Alle | Deaktiviert (Text) | onSurfaceVariant |

---

## Banner-Tokens

| ID | Rolle | Beschreibung | Farbe |
|---|---|---|---|
| banner.{role}.background | Alle | Hintergrund | {role}.container |
| banner.{role}.title | Alle | Überschrift | {role}.accent |
| banner.{role}.body | Alle | Text-Body | {role}.onContainer |
| banner.{role}.icon | Alle | Icon-Farbe | {role}.accent |
| banner.{role}.actionContent | Alle | Button-Text | {role}.accent |

---

## Badge-Tokens

| ID | Rolle | Beschreibung | Farbe |
|---|---|---|---|
| badge.{role}.countBg | Alle | Count Badge (Hintergrund) | {role}.accent |
| badge.{role}.countContent | Alle | Count Badge (Zahl) | {role}.onAccent |
| badge.{role}.pillBg | Alle | Pill Badge (Hintergrund) | {role}.container |
| badge.{role}.pillContent | Alle | Pill Badge (Text) | {role}.accent |

---

## Shape-Tokens (Corner Radius)

| ID | Beschreibung | Wert |
|---|---|---|
| shape.card | Karten, Banner, Menü, Container | 20.dp |
| shape.button | Button, Extended FAB | 20.dp |
| shape.chip | Filter- und Input-Chips | 6.dp |
| shape.textField | Einzeiliges Textfeld | 14.dp |
| shape.sheet | Bottom Sheet (obere Ecken) | 28.dp |
| shape.dialog | Dialog | 28.dp |
| shape.pill | Status-Pill, Count-Badge | 999.dp |

---

## Spacing-Tokens (8 dp Raster)

| ID | Beschreibung | Wert |
|---|---|---|
| spacing.xs | Minimal | 4.dp |
| spacing.sm | Klein | 8.dp |
| spacing.md | Mittel | 12.dp |
| spacing.lg | Groß (Standard) | 16.dp |
| spacing.xl | Extra Groß | 24.dp |
| spacing.xxl | Doppelt extra Groß | 32.dp |

### Standard-Zuordnung
- Karten-Innenabstand: `lg` (16 dp)
- Abstand zwischen Karten: `md` (12 dp)
- Abschnittsabstand: `lg` (16 dp)
- Chip-Innenabstand: `sm` (8 dp)
- Icon-zu-Text: `sm` (8 dp)
- Screen-Rand: `lg` (16 dp)

---

## Tipps für schnellere Referenzierung

**Kurzschreibweise:**
- `primary.accent` statt `role.primary.accent`
- `error.container` statt `role.error.container`
- `surface.onSurface` statt `surface.onSurface`
- `button.primary.tonalBg` für Button-Hintergrund in Tonal-Variante

**Nach Rolle referenzieren:**
```
AppTheme.tokens.button.forRole(AppRole.Primary).tonalBackground
AppTheme.colors.forRole(AppRole.Error).accent
```

**MaterialTheme direkt (für M3-Defaults):**
```
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.error
```
