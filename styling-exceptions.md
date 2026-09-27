# Styling-Ausnahmen

## Custom-Elemente

| Element | Datei | Kriterium (Abschnitt 7) | Begründung |
|---|---|---|---|
| Showcase-Nachbauten (Buttons, Chips, Karten, Badges, Listenzeilen, Banner als gezeichnete Referenz) | `app/.../ui/showcase/Section*.kt`, `ShowcaseCommon.kt`, `ShowcaseListPrimitives.kt`, `ShowcaseSettingsMocks.kt` | Technische Unmöglichkeit | Der Showcase ist die visuelle Spezifikation des Designsystems (HTML v2.1) und zeigt alle sieben Rollen in allen Varianten, inklusive deaktivierter Zustände und Inspektor-IDs pro Element. Die Library-Bausteine bieten weder die Rolle Success noch IDs an jedem Teilelement. |
| Diagramm-/Ramp-Swatches | `app/.../ui/showcase/Section03Ramps.kt`, `Section04Surfaces.kt` | Datenvisualisierung | Farbfelder zeigen Palette-Werte. |

## Abweichungen vom Basis-Projekt

| Thema | Basis-Projekt | Diese App | Grund |
|---|---|---|---|
| Library | Baustein-Sammlung `uikit` mit `AppTheme.tokens` | `lib/*` aus Periodical, Theme über `lib:settings` und `MaterialTheme.colorScheme` | Library ist identisch mit Periodical, Basis-Projekt ist jetzt deren Quelle |
| Rolle Success | Sieben Rollen im Theme | Success als eigene Token-Gruppe (`SuccessColorTokens`), Buttons der Library kennen nur sechs Rollen | Library hat kein Success-Token |
| Flächen | `surface`/`surfaceVariant`/`surface3` unterschiedlich | Alle Flächenrollen sind Alias auf `background` (`MaterialRoleMapping`) | Entscheidung der Library: flache, vorhersehbare Flächen |
