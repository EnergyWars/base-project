# WaffleHQ Farbsystem

## Überblick

Das Farbsystem stammt aus der Library `:lib:settings` (`lib/settings/.../colors/`) und ist identisch mit dem von Periodical. Die App liefert nur ihren Token-Katalog, die Zuordnung auf die Material-Rollen und das Theme-Composable.

| Baustein | Ort |
|---|---|
| Sieben Hue-Rampen (10–90), Flächen-/Ink-/Hairline-Konstanten | `lib/settings/.../colors/ColorPalette.kt`, `ColorRampTable.kt` |
| Token-Registry, Resolver, Override-Store, Themes, Export/Import | `lib/settings/.../colors/*` |
| Farbeinstellungs-UI (Kategorien, Picker, Kontrast-Badges, Sicherheits-Overlay) | `lib/settings/.../colors/ui/*` |
| App-Token-Katalog (Global, Success) | `app/.../domain/colortheme/` |
| Theme-Composable `BaseAppTheme`, `AppThemedContent`, `colorToken(...)` | `app/.../ui/theme/` |
| Rollenfarben für den Showcase (`AppTheme.colors`, 7 Rollen) | `app/.../ui/theme/AppTheme.kt` |
| Geist / Geist Mono | `app/.../ui/theme/Type.kt`, `app/src/main/res/font/` |

## Regeln

- Rohe Rampen-Töne (`Sapphire40` …) und Flächenkonstanten nur in `ColorPalette.kt`, `ColorRampTable.kt`, `Theme.kt` und den Token-Katalogen.
- Keine Hex-Literale, keine `Color.Red`-artigen Konstanten in UI-Code.
- Farben im UI-Code über `MaterialTheme.colorScheme.*`, `AppTheme.colors.*` oder `colorToken(...)`.
- Modul- oder Bereichsfarben als eigenes Farb-Token im App-Katalog registrieren (Kategorie in `ColorTokenCategory`, Tokens in `domain/colortheme/tokens/`, Label-Strings in beiden `strings.xml`). Resolver, Persistenz und Settings-UI bleiben unverändert.
- Success ist keine Material-Rolle; die App führt sie als eigene Token-Gruppe (`SuccessColorTokens`).

## Validierung

```bash
./verify-theme.sh
scripts/validate-colors.sh
```

`verify-theme.sh` prüft Token-Isolation, Hex-Freiheit und die SHA-256-Prüfsummen von `ColorPalette.kt` und `Theme.kt` (`theme-hashes.sha256`). Nach `scripts/sync-lib.sh` die Prüfsummen neu erzeugen:

```bash
sha256sum lib/settings/src/main/kotlin/com/wafflehq/lib/settings/colors/ColorPalette.kt app/src/main/java/com/wafflehq/base/ui/theme/Theme.kt > theme-hashes.sha256
```

## Neue App aus dem Template

1. `app/` kopieren, Paket/`applicationId`/`app_name` anpassen.
2. Token-Katalog um die App-eigenen Kategorien erweitern.
3. `verify-theme.sh` und `theme-hashes.sha256` mitnehmen, Pfade in `verify-theme.sh` (`APP_NAME`) anpassen.
