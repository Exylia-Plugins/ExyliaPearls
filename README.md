# ExyliaPearls

Free, open-source ender pearl fix for Paper and Folia. A pearl never puts a player inside a block: while it flies, it remembers the last spot where its thrower would fit, and if it lands somewhere they would clip into terrain, they go to that spot instead. A safe landing is left entirely to vanilla.

## Requirements

- Paper or Folia 1.21+, Java 21
- [ExyliaLib](https://modrinth.com/plugin/exylialib) ([GitHub releases](https://github.com/DiGround-s/ExyliaLib/releases)). Install it first; without it the plugin stays off and says where to get it.

## Install

1. Download `ExyliaPearls.jar` from the [latest release](https://github.com/Exylia-Plugins/ExyliaPearls/releases/latest).
2. Put it in `plugins/` and restart the server.

## Updates

ExyliaLib checks this plugin's GitHub releases and announces a new version in the console and to admins. Nothing is downloaded on its own: run `/exylialib plugins` to download the update, which is applied on the next restart. Tune it under `plugin-updates` in `plugins/ExyliaLib/config.yml`.

## Configuration

`config.yml`:

- `ender-pearls.mode`: how much of the player has to fit for a landing to count as safe. `FULL` (the whole body), `PARTIAL` (sneak height), `HALF` (one block) or `NORMAL` (recommended).
- `ender-pearls.check-tick-interval`: how often, in ticks, a pearl in flight is checked. Keep it at 1 unless it costs you performance.

Messages live in `lang/<code>/messages.yml` (English, Spanish, Portuguese, French, Russian and Turkish); `language` in `config.yml` picks one, or `default` follows ExyliaLib.

## Commands

| Command | Permission | |
| --- | --- | --- |
| `/pearls debug` (`/exyliapearls`) | `exyliapearls.admin` | Toggles drawing the collision boxes pearls are checked against |
| `/pearls reload` | `exyliapearls.admin` | Reloads the config and messages |

## API

Other plugins can read tracked pearls, and refuse or redirect a rescue, through `PearlsService` on the Exylia API in ExyliaLib.

## Building

```
./gradlew build
```

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[GNU General Public License v3.0](LICENSE).
