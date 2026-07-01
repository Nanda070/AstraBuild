# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Deploy

`ash
# Windows
gradlew.bat build

# Unix
./gradlew build
`

uild compiles the plugin and automatically SFTPs the JAR to the configured server via deploy.gradle if local.properties is present.

**CRITICAL ENCODING RULE:** The codebase contains extensive Cyrillic (Russian) UI strings. The Java compiler must treat all files as UTF-8. uild.gradle.kts enforces this via 	asks.withType<JavaCompile> { options.encoding = "UTF-8" }. When editing GUI text, **NEVER** use tools or scripts that write files with ANSI/Windows-1251 encoding. 

## Architecture

AstraBuild is a **PaperMC 1.21 plugin** (Java 21, Paperweight userdev). It adds industrial machines, energy networks, logistics pipes, quarrying, orbital strikes, and nano equipment.

### Key Components

- **MachineDataStorage:** Wraps PersistentDataContainer (PDC). All machine states (energy, coolant, type, bounds) live in PDC on the block's TileState.
- **IndustrialManager:** Orchestrates single-threaded bukkit tasks for Reactor, Centrifuge, Mass Fabricator, Energy Storage, Coolant Tank.
- **Holograms:** TextDisplay entities tagged with strabuild_holo. Cleaned up and updated dynamically by HologramManager.

### Energy System

Energy (EU) is stored as an INTEGER in each block's PDC.
Charging happens in EnergyStorageWorker, which also charges items inside its GUI (Slot 0 for internal transfer, visual in Slot 11). Rate is typically 50 EU/tick for items to allow smooth visual updates.

### Pipe Network & Item Logistics

PipeNetwork maintains a BlockPos -> NetworkNode graph. Nodes have specific types (NetworkNode.NodeType):
- PIPE_ENERGY (Yellow Stained Glass Pane): Translates energy.
- PIPE_ITEM (Light Blue Stained Glass Pane): Translates items from Quarry to Connectors.
- CONNECTOR_INSERT, CONNECTOR_EXTRACT: Link pipes to external containers (e.g. Chests).
- MACHINE_QUARRY, MACHINE_SATELLITE: Machine nodes that connect to networks.

getConnectedNodes() and indShortestPath() accept an allowed NetworkNode.NodeType to restrict pathfinding (e.g., energy packets cannot travel through item pipes).

### Radiation System

- RadiationManager sets radiation per chunk using a LONG timestamp key chunk_radiation_end_time in the chunk's PDC.
- **Radiation Cleaner:** A Beacon block that, when placed, instantly cleans radiation within a 100-block radius (cleanArea()) and destroys itself, playing an explosion sound/particle.

### GUI System

Inventory changes are synchronized back to the block's PDC on the next tick via scheduleGuiSync() to avoid same-tick race conditions with the worker ticker. Strings for GUIs must be valid UTF-8.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
