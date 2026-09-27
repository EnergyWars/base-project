# Basisprojekt

Vorlage für WaffleHQ-Android-Projekte. Enthält die generische Library (`lib/`, importierbar in andere Projekte) und eine Beispiel-App (`app/`), die sie nutzt. Features stehen in `FEATURES.md`, der aktuelle Stand in `context.md`.

## Struktur

- `lib/<modul>`: 1:1-Kopie des Library-Teils von `../periodical/lib` (Package `com.wafflehq.lib.*`, ohne die app-spezifischen Module `domaincore` und `todocore`, ohne Code-Kommentare). **Nie von Hand ändern.** Aktualisieren mit `scripts/sync-lib.sh`, danach `python3 scripts/check_imports.py lib` und `./verify-theme.sh`.
- `app/`: Beispiel-App (Hilt, Room, DataStore, Compose Navigation), verdrahtet Theme, Farb-Tokens, Settings und Navigation mit der Library und zeigt Showcase und Library-Beispiele.
- Import in andere Projekte: `docs/LIB-IMPORT.md`.

## Regeln

- Design: Skill `base-project`, Styling-Regeln aus der globalen Konfiguration. Abweichungen und Custom-Elemente stehen in `styling-exceptions.md`.
- Farben nur über Theme/Tokens (`docs/COLOR-SYSTEM.md`), keine Hex-Literale.
- Strings: Deutsch zuerst (`values-de`), Englisch als Fallback (`values`); nie hartcodieren.
- Showcase-IDs: `SHOWCASE-ELEMENT-IDS.md` bei jeder ID-Änderung im Showcase nachziehen.
- Beim Kopieren als Vorlage: `applicationId`, `namespace`, Package-Pfad (`com.wafflehq.base`), `rootProject.name`, `app_name`, Launcher-Icon, `PlaceholderEntity`/DB-Version, eigene `FEATURES.md` und `context.md`.

## Build

`gradle build` oder `assembleDebug` niemals ohne explizite Aufforderung ausführen.
