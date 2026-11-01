# Changelog

## 1.1.1

- Fixed explosions still destroying protected blocks when Lithium (or another mod replacing
  vanilla's explosion ray casting) is installed. The blast resistance is now applied where every
  explosion reads it, and protected blocks are removed from the final list of destroyed blocks
  as a last safety net.
- Added the mod icon.
- Rewrote the README.

## 1.1.0

- Separate hardness values for spawners, trial spawners and vaults, each with its own value per
  difficulty.
- Config files from 1.0.0 are migrated automatically: their values are copied to all three blocks.

## 1.0.0

- Spawners, trial spawners and vaults are indestructible like bedrock: no mining outside
  creative, no explosions, no Wither, no Ender Dragon.
- Hardness configurable per difficulty (-1 = indestructible, default everywhere).
- Optional Mod Menu + Cloth Config screen for singleplayer and LAN worlds.
