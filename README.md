# CustomName v2

Custom Names **und Prefixe** mit vollem MiniMessage- und Folia-Support.

## Befehle

| Befehl | Beschreibung | Permission |
|---|---|---|
| `/customname` | Öffnet das GUI | `customname.use` |
| `/customname <MiniMessage>` | Setzt den Namen | `customname.use` / `customname.nick` |
| `/customname reset` | Name zurücksetzen | `customname.use` |
| `/prefix` | Öffnet das GUI | `customname.prefix.use` |
| `/prefix <MiniMessage>` | Setzt das eigene Prefix | `customname.prefix.use` |
| `/prefix reset` | Entfernt das eigene Prefix | `customname.prefix.use` |
| `/prefix set <Spieler> <Prefix>` | Prefix eines anderen setzen | `customname.prefix.others` |
| `/prefix reset <Spieler>` | Prefix eines anderen entfernen | `customname.prefix.others` |
| `/prefix reload` | Configs neu laden | `customname.admin` |

### Beispiel

```
/prefix <b><gradient:#7688FF:#9584FF><shadow:#183931:1>Gooner</shadow></gradient></b>
```

Das Prefix wird vor dem Namen in Chat-Anzeigenamen und Tab-Liste gesetzt.
`<shadow>` benötigt Minecraft **1.21.4+** (Adventure 4.18+). Auf älteren
Servern wird der Tag automatisch entfernt, statt als Text angezeigt zu werden.

## GUI

* Haupt-GUI standardmäßig **6 Reihen** (`gui.rows`).
* **Vorschau-Item**: Slot und Material frei einstellbar unter `gui.items.preview`
  (ebenso im Farb-Menü unter `color-menu.preview`).
* **Schließen-Button**: `gui.items.close` (Slot/Material/Name konfigurierbar).
* **Keine fest einprogrammierten Item-Beschreibungen mehr** – Lore kommt zu 100 %
  aus der `config.yml`. Leere `lore: []` = gar keine Lore. Zusätzliche
  Vanilla-Tooltips (Attribute etc.) werden ausgeblendet.
* Jeder Button hat `enabled`, `slot`, `material`, `name`, `lore`, `glow`.

### Farben ergänzen

Im Selector-GUI (`color-menu.colors`) können beliebig viele Farben ergänzt werden:

```yaml
color-menu:
  colors:
    - name: '<gradient:#7688FF:#9584FF>Gooner</gradient>'
      material: AMETHYST_SHARD
      format: '<b><gradient:#7688FF:#9584FF><shadow:#183931:1><player></shadow></gradient></b>'
      slot: 22          # optional, sonst automatische Platzierung
      permission: ''    # optional
      lore: []          # optional
```

`<player>` wird durch den Spielernamen ersetzt.

### Platzhalter in GUI-Texten

`<player>`, `<name>`, `<prefix>`, `<raw_name>`, `<raw_prefix>`, `<preview>`

## PlaceholderAPI

`%customname_name%`, `%customname_nameonly%`, `%customname_raw%`,
`%customname_prefix%`, `%customname_prefix_raw%`, `%customname_hasprefix%`,
`%customname_isnick%`

## Folia

Alle Aufgaben laufen über `SchedulerUtil` (GlobalRegionScheduler /
EntityScheduler / AsyncScheduler auf Folia, klassischer BukkitScheduler
auf Paper/Spigot). Dateien werden asynchron gespeichert.

## Build

```
mvn clean package
```

Benötigt JDK 21 (Paper API 1.21.4).
