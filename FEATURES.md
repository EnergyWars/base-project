# Features

## Library (`lib/`, importierbar)

Generische, app-neutrale Module aus Periodical (Package `com.wafflehq.lib.*`), siehe `docs/LIB-IMPORT.md`.

| Modul | Features |
|---|---|
| `ui-core` | Buttons (Rollen Primary/Secondary/Tertiary/Warning/Error/Neutral, Varianten Filled/Tonal/Elevated/Outlined/Text), FABs, Karten, Dialoge (Bestätigen, Löschen, Verwerfen, Namenseingabe, Fehlerdetails), Chips, Banner, Listenzeilen, Pill-Tabs, Fortschritt, Auswahl-Steuerelemente, Slider, Snackbar, Suchfeld, Stepper, Kennzahlen, Leerzustände, Wochentags-Chips, Bottom Sheets, Dropdowns/Autocomplete/Popup-Menüs, Drag-&-Drop-Listen, Breadcrumb, Icon-Picker, Farbpicker, Scaffold mit einklappbarer Titelleiste, Gesten (Long-Press, Auto-Scroll, Zoom, Sheet-Swipe, Tap-Flash), CSV (Kodierung, Parser, Formel-Schutz, Export-/Vorschau-Dialoge), Zeit-/Datumsformate, Finanzformatierung, Offscreen-Rendering, Enum-Codecs, SQL-LIKE-Escaping |
| `navigation` | Settings-Startseite, Settings-Bausteine, App-Shell mit Top-Navigation und Seitenmenü (Suche, Pins, Zuletzt-Verlauf), Editor-Gerüst mit Entwurfs-Schutz, Vollbild-Unterseiten |
| `settings` | Farbsystem (Palette, Token-Registry, Resolver, Overrides, mehrere Themes, Export/Import, Sicherheits-Rücksetzung, Kontrast-Badges, Picker), Verschlüsselungs-Einstellungen, Google-Drive-Backup, Datenlöschung, Rechtstexte, Open-Source-Lizenzen, Modul-Onboarding |
| `folders` | Ordnerbaum, Ordner-Karten, Eintrags-Karten, Drag & Drop, Aussehen (Icon/Farbe), Lösch-Aktionen |
| `entrylock` | Eintrags-Sperre mit Biometrie, Sperr-Zustandsmaschine, Sperr-UI |
| `quickpicker` | Ziffern-Eingabe für Datum/Uhrzeit, Monats-/Wochen-/Jahres-Auswahl, konfigurierbare Datumsfelder |
| `textarea` | Tastaturbewusstes Mehrzeilenfeld |
| `drafts` | Entwurfs-Autosave |
| `modules` | Feature-Modul-Registry, Berechtigungskatalog, Tages-Marker |
| `maintenance` | Gebündelte periodische Hintergrundaufgaben, Zeitplanberechnung |
| `database` | SQLCipher-Verschlüsselung, Schlüsselverwaltung, Konvertierung |
| `backupcore` | Backup-Aufbewahrung, Versionsprüfung |
| `prefsbackup` | Preferences-Backup-Codec |
| `pdf` | PDF-Seiten-Layout, Wasserzeichen, Export-/Vorschau-Dialoge, Speichern/Teilen |
| `charts` | Linien-, Balken-, Radar-Diagramme mit Zoom und Tooltips |
| `media` | Foto-/Audio-Ablage, EXIF-Bereinigung, aufrechte Bitmaps |
| `diagnostics` | Diagnose-Logger, Freeze-Erkennung, Crash-Logger, Lifecycle-Logger |
| `notifications` | Benachrichtigungskanäle, Sperrbildschirm-Schutz |
| `astronomy` | Sonnen-/Mondberechnung |
| `qr` | QR-Code-Erzeugung |

## Beispiel-App (`app/`)

- **Showcase (Startseite):** 33 Sektionen des WaffleHQ-Designsystems (Typografie, Rampen, Oberflächen, Rollen, Buttons, FAB, Icon-Buttons, Chips, Textfelder, Karten, Listen, Auswahl, Segmented, Slider/Fortschritt, Badges, Banner, Snackbar/Dialog, Icons, Trenner, Abstände, App-Header, Settings, Listenvarianten, Container) mit Hell/Dunkel-Umschalter.
- **Element-Inspektor:** Doppeltipp auf ein Showcase-Element zeigt dessen ID (kopierbar); IDs siehe `SHOWCASE-ELEMENT-IDS.md`.
- **Bibliotheken-Beispiele:** je Lib-Modul eine lauffähige Demo (24 Abschnitte), durchsuchbar.
- **Seitenmenü und Titelleiste:** Navigation zwischen Showcase, drei Beispielseiten, Bibliotheken-Beispielen und Einstellungen.
- **Einstellungen:** Anzeige (Theme-Modus System/Hell/Dunkel, persistiert), Farben (Kategorien, Token-Editor, Themes, Import/Export über `lib:settings`), Feature-Liste.
- **Feature-Liste:** Markdown-Dateien aus `features/*.md` (per Gradle-Task in die Assets kopiert) mit Detailansicht und abhakbaren/ausblendbaren Einträgen.
- **Theme:** Farb-Tokens (Global, Success), Geist/Geist Mono, Material-Zuordnung, Laufzeit-Overrides.
- **Infrastruktur:** Hilt, Room (Platzhalter-Entity), DataStore-Einstellungen.
