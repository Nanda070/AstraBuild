# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Deploy

```bash
# Windows
gradlew.bat build

# Unix
./gradlew build
```

`build` compiles the plugin and, if `local.properties` is present, automatically SFTPs the JAR to the configured server via `deploy.gradle`. Output: `build/libs/AstraBuild-1.0.0-SNAPSHOT-dev.jar`.

First-time NMS setup (Paperweight mojmap):
```bash
./gradlew setupLocalDebugServer
```

`local.properties` (gitignored) must contain SFTP credentials for deployment:
```
sftp.host=...
sftp.port=...
sftp.user=...
sftp.password=...
sftp.dir=...
```

There are no automated tests.

## Architecture

AstraBuild is a **PaperMC 1.21 plugin** (Java 21, Paperweight userdev) that adds industrial machines, energy networks, logistics pipes, quarrying, orbital strikes, and nano equipment to the Astra SMP network. It hard-depends on the `AstraSMP` plugin (declared in `plugin.yml`).

### Plugin Initialization Order (`AstraBuildPlugin.java`)

Subsystems initialize in strict dependency order on `onEnable()`:

1. `MachineDataStorage` — PDC wrapper; must come first (all other systems read/write through it)
2. `DeliveryService` + `PipeNetwork` — logistics layer
3. `EventManager` — global timed events that apply fuel-value multipliers; persists state in `config.yml`
4. `IndustrialManager` — tick orchestrator for Reactor, Centrifuge, Mass Fabricator, Energy Storage, Coolant Tank
5. `QuarryManager`, `RecipeRegistry`, `RadiationManager`, `HologramManager`
6. Listeners: `BlockListener`, `ChunkScanner`, `GuiListener`, `OrbitalStrikeListener`, `NanoEquipmentListener`

### Machine Lifecycle

**Place:** `BlockListener` detects a custom machine item → writes machine type via `MachineDataStorage.setMachineType()` (PersistentDataContainer on the TileState) → registers the location as a node in `PipeNetwork` → creates a Worker instance in `IndustrialManager`.

**Tick:** `IndustrialManager` runs a single BukkitTask every server tick. Each registered worker's `runTick()` reads/writes energy from the block's PDC (`machine_energy` INTEGER key), consumes inputs, produces outputs, and updates its floating hologram.

**Break:** `BlockListener` stops the worker, deregisters from `PipeNetwork`, clears PDC keys, removes the hologram entity, and drops the custom item.

**Restart persistence:** `ChunkScanner` re-registers machines on chunk load by scanning loaded blocks for the `machine_type` PDC key. Quarry bounds are also persisted to `quarries.yml` via `QuarryManager`.

### Energy System

Energy (EU) is stored as an INTEGER in each block's PDC. Machine capacities:

| Machine | Capacity |
|---|---|
| Quarry / Centrifuge | 10,000 EU |
| Mass Fabricator | 1,000,000 EU |
| Reactor | 100,000 EU |
| Energy Storage | 10,000,000 EU |
| Satellite Terminal | 36,000,000 EU |

`EnergyStorageWorker` and `NuclearReactorCore` distribute energy every 20 ticks to all reachable machines found via `PipeNetwork.getConnectedNodes()` (flood-fill BFS). Reactors and other batteries are excluded from receiving energy to prevent feedback loops.

The Reactor also manages a heat system (0–10,000): it gains heat per tick and cools via nearby water/ice blocks and `CoolantTankWorker`. At 10,000 heat it triggers a meltdown (destroys core, 24-hour chunk radiation via `RadiationManager`).

### Pipe Network & Item Logistics

`PipeNetwork` maintains a `BlockPos → NetworkNode` graph where nodes are PIPE, CONNECTOR_INSERT, or machine locations. Item routing uses BFS capped at 500 visited nodes. `DeliveryService` walks the resolved path and delivers to the container facing the target `CONNECTOR_INSERT` (Dropper orientation determines direction). Fallback: connector inventory → world drop.

Animated item-in-transit visuals are handled by `DisplayUtil` (spawns an `ItemDisplay` entity that teleports per tick, interpolated on the client side).

### GUI System

Right-clicking a machine opens an inventory-based GUI. `GuiListener` reads the machine type from storage and delegates to the appropriate `*Gui` class (`gui/` package). Inventory changes are synchronized back to the block's PDC on the next tick via `scheduleGuiSync()` to avoid same-tick race conditions with the worker ticker.

### Key Design Notes

- **All machine state lives in PDC** — no external database. Machine type, energy, upgrades (Fortune/Silk Touch/Overclocker), and quarry bounds are all stored as PersistentDataContainer keys on the block entity (TileState).
- **`IndustrialManager` uses `ConcurrentHashMap`** for its worker maps to avoid `ConcurrentModificationException` during single-threaded tick iteration.
- **`PipeNetwork` BFS has a hard 500-node cap** as DDoS protection against pathologically large networks.
- **Fortune and Silk Touch upgrades are mutually exclusive** (enforced in `MachineDataStorage`); Overclocker stacks with either.
- **Holograms** are `TextDisplay` entities tagged with `astrabuild_holo` PDC key; `HologramManager` cleans up abandoned duplicates within a 2-block radius on each update.
