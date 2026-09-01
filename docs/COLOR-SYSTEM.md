# WaffleHQ Design System — Color Compliance

## Überblick

Die Color-System ist im Projekt vollständig validiert und erzwungen:

- ✓ **Color.kt** enthält alle Farb-Tokens aus dem WaffleHQ v2 Design System
- ✓ **Theme.kt** ist der einzige Ort, wo Color.kt importiert wird
- ✓ Alle UI-Screens nutzen **nur** `MaterialTheme.colorScheme` und `AppTheme.extendedColors`
- ✓ Keine hardcodierten Hex-Werte außerhalb von Color.kt/Theme.kt

## Struktur

```
ui/theme/
├── Color.kt          ← Alle Farb-Token-Definitionen (Hues 10–90)
├── Type.kt           ← Geist & Geist Mono Font-Konfiguration  
└── Theme.kt          ← ColorScheme, ExtendedColors, ColorRamps
```

### Color.kt

Enthält:
- **5 Hue-Ramps:** Sapphire, Emerald, Amethyst, Citrine, Garnet (je 10, 20, 30, 40, 80, 90)
- **Surfaces:** DarkBackground, DarkSurface, DarkSurfaceVariant, DarkSurface3, LightBackground, LightSurface, LightSurfaceVariant, LightSurface3
- **Ink/OnSurface:** OnSurfaceDark, OnSurfaceLight, OnSurfaceVariantDark, OnSurfaceVariantLight, OutlineDark, OutlineLight
- **Period-tracker:** PeriodActualLight, PeriodActualDark, PeriodPredicted

### Theme.kt

Exportiert:
- **`AppTheme.extendedColors`:** Warning-Slot (Citrine) für Material 3
- **`AppTheme.colorRamps`:** Alle Farb-Ramps für Showcase-Screens

### Type.kt

Geist & Geist Mono werden von Google Fonts bezogen:
- **GeistSans:** Light, Normal, Medium, SemiBold, Bold
- **GeistMono:** Normal, Medium, SemiBold
- **Tabular numerals:** `tnum` Feature aktiviert auf allen Text-Stilen
- **Negative tracking:** Display (-0.025em), Headline (-0.022em)

## Validierung

### Manuell ausführen
```bash
scripts/validate-colors.sh
```

### Automatisch vor Commit
Ein Git pre-commit Hook prüft automatisch:
1. ✓ Color.kt nur in Theme.kt importiert
2. ✓ Keine direkten Farb-Token-Imports außerhalb Theme.kt
3. ✓ Keine hardcodierten Hex-Werte (`Color(0xFF...)`)
4. ✓ Keine ungültigen Color.*-Patterns (Color.Red, Color.Green, etc.)
5. ✓ Alle erforderlichen Farb-Tokens vorhanden
6. ✓ Fonts (Geist/Geist Mono) konfiguriert

## Richtig: Farben im App-Code nutzen

```kotlin
// ✓ RICHTIG: Theme verwenden
Text(
    text = "Hello",
    color = MaterialTheme.colorScheme.primary,
    style = MaterialTheme.typography.bodyLarge,
)

// ✓ RICHTIG: ExtendedColors für Warning
val ext = AppTheme.extendedColors
Box(
    modifier = Modifier.background(ext.warning)
)

// ✓ RICHTIG: Nur für Showcase (HomeScreen)
val ramps = AppTheme.colorRamps
ramps.forEach { ramp -> /* ... */ }
```

## Falsch: Was man vermeiden muss

```kotlin
// ✗ FALSCH: Direct Color.kt Import
import com.wafflehq.base.ui.theme.Sapphire40

// ✗ FALSCH: Hardcodierte Hex-Werte
color = Color(0xFF0E3D6E)

// ✗ FALSCH: Android Color.*
color = Color.Blue  // ← nur Color.Transparent/White erlaubt
```

## Für neue Apps: Template-Anpassung

Beim Kopieren dieser Base-App als Template:

1. `Color.kt` bleibt unverändert (alle Farbs sind bereits da)
2. `Type.kt` bleibt unverändert (Geist ist bereits konfiguriert)
3. `Theme.kt` anpassen, falls App-spezifische Extended Colors nötig sind
4. Pre-commit Hook in neue App kopieren: `.git/hooks/pre-commit`
5. Validierungs-Script kopieren: `scripts/validate-colors.sh`

## FAQ

**Q: Ich brauche eine Farbe, die nicht im System ist.**  
A: Neue Farben können nur in `Color.kt` hinzugefügt werden. Konsultiere zuerst den `/wafflehq-design` Skill — die Ramps sollten komplett sein.

**Q: Wie nutze ich Farben in Custom Components?**  
A: Nutze immer Parameter vom Typ `Color`, pass `MaterialTheme.colorScheme.*` oder `AppTheme.extendedColors.*` rein.

**Q: Was ist PeriodActualLight/Dark/Predicted?**  
A: Farben für Periodical (WaffleHQs Period-Tracking-App). In anderen Apps kannst du sie ignorieren oder für ähnliche Nutzungen nutzen.

**Q: Der pre-commit Hook blockiert meinen Commit!**  
A: Laufe `scripts/validate-colors.sh` und fix die Fehler. Hook sichert Color-System-Compliance.

## Referenz

- **Design System:** `/wafflehq-design` Skill
- **Color Ramps:** `ui/theme/Color.kt`
- **Active Theme:** `MaterialTheme` + `AppTheme`
- **Fonts:** Google Fonts (Geist)
