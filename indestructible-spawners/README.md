# Indestructible Spawners

Spawners, trial spawners and vaults become as indestructible as bedrock.
Or give each of them a custom hardness, set separately for every difficulty.

**Server-side only.** Install it on the server, or in your own game for
singleplayer and LAN worlds. Players don't need it to join.

## Features

By default, in every difficulty, monster spawners, trial spawners and vaults:

- **can't be mined** outside creative mode: the mining progress stops right
  away, just like on bedrock;
- **resist every explosion** with bedrock's blast resistance: TNT, creepers,
  end crystals, beds, respawn anchors, ghast fireballs and Wither skulls,
  blue ones included. Like bedrock, they also shield the blocks behind them;
- **can't be destroyed by the Wither** charging through blocks;
- **can't be destroyed by the Ender Dragon** flying through them.

Creative players can still break them, as with bedrock.

## Configuration

Each block (spawner, trial spawner, vault) has one hardness value per
difficulty:

| Value | Effect |
| --- | --- |
| `-1` | Indestructible, as described above (default everywhere) |
| `0` or more | Hardness used for mining, on the vanilla scale. Explosions, the Wither and the Ender Dragon then behave as in vanilla |

For reference, vanilla hardness is **5** for spawners and **50** for trial
spawners, vaults and obsidian. `0` breaks the block almost instantly (the
mining speed multiplier is capped at x1024, the maximum of the attribute).
Hardcore worlds use the Hard value, and changing the difficulty with
`/difficulty` takes effect immediately.

> Trial spawners and vaults already have a blast resistance of 50 in vanilla,
> so TNT and creepers can't destroy them even with a finite hardness.

### Singleplayer and LAN

With [Mod Menu](https://modrinth.com/mod/modmenu) and
[Cloth Config](https://modrinth.com/mod/cloth-config) installed, every value
can be edited in game, with one section per block. Changes apply immediately.
Both mods are optional.

### Dedicated server

Edit `config/indestructiblespawners.json`, then restart the server:

```json
{
  "spawner": {
    "peaceful": -1.0,
    "easy": -1.0,
    "normal": -1.0,
    "hard": -1.0
  },
  "trial_spawner": {
    "peaceful": -1.0,
    "easy": -1.0,
    "normal": -1.0,
    "hard": -1.0
  },
  "vault": {
    "peaceful": -1.0,
    "easy": -1.0,
    "normal": -1.0,
    "hard": -1.0
  }
}
```

Invalid values (below -1, or between -1 and 0) and missing blocks are replaced
by the default and reported in the server log. A config file from version
1.0.0, which had a single set of values, is migrated automatically: its
values are copied to all three blocks.

## Compatibility

- Minecraft **1.21.2 to 1.21.11** with Fabric Loader 0.20.1 or newer.
- **Fabric API is not required.**
- Compatible with Lithium's explosion optimizations.
- Other mods that change the `block_break_speed` attribute keep working: the
  mod's multiplier stacks with theirs.

## How it works

- **Mining**: the block's hardness itself is never changed, since vanilla
  clients would keep using the vanilla value and desync (blocks breaking, then
  reappearing). Instead, while a player mines a protected block, their
  `block_break_speed` attribute gets a temporary multiplier of
  `vanilla hardness / configured hardness` (0 when indestructible).
  Attributes are synced to the client, so both sides compute exactly the same
  progress. The multiplier is removed as soon as the player stops mining.
- **Server-side guarantee**: on top of that, the server refuses to break an
  indestructible block for a player outside creative, whatever the client
  sends.
- **Explosions**: indestructible blocks get bedrock's blast resistance where
  every explosion reads it, including mods that replace the vanilla explosion
  code. As a last safety net, they are also removed from the final list of
  blocks an explosion destroys.
- **Wither and Ender Dragon**: vanilla protects blocks from them with the
  `#minecraft:wither_immune` and `#minecraft:dragon_immune` tags, but tags
  can't depend on the difficulty, so the mod adds its own check next to them.

## Known limitation

The mining speed multiplier reaches the client after a network round trip.
With very fast mining (Efficiency V netherite pickaxe plus Haste II) and a
high ping, the client can predict a spawner breaking before it receives the
new speed. The server refuses and the spawner comes back, but the client may
show an empty cage, without the spinning mob, until the chunk reloads. This is
purely visual: the spawner keeps working.

## Building from source

```sh
./gradlew build
```

The jar is written to `build/libs/`. Use `./gradlew runClient` to test in a
development client, which includes Mod Menu and Cloth Config.

## License

[MIT](LICENSE)
