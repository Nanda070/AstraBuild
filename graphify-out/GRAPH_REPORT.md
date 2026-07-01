# Graph Report - c:/Users/adnan/Documents/coding/AstraBuild  (2026-07-01)

## Corpus Check
- Corpus is ~17,621 words - fits in a single context window. You may not need a graph.

## Summary
- 547 nodes · 1426 edges · 26 communities
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 174 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Core Plugin and Listeners|Core Plugin and Listeners]]
- [[_COMMUNITY_Events and Quarry Worker|Events and Quarry Worker]]
- [[_COMMUNITY_Recipe and GUI Listeners|Recipe and GUI Listeners]]
- [[_COMMUNITY_Item Recipe Registry|Item Recipe Registry]]
- [[_COMMUNITY_Core GUI Implementations|Core GUI Implementations]]
- [[_COMMUNITY_Orbital Strike Mechanism|Orbital Strike Mechanism]]
- [[_COMMUNITY_Centrifuge and Mass Fabricator|Centrifuge and Mass Fabricator]]
- [[_COMMUNITY_Energy Storage and Uranium|Energy Storage and Uranium]]
- [[_COMMUNITY_Coolant Tank Components|Coolant Tank Components]]
- [[_COMMUNITY_Plugin Commands|Plugin Commands]]
- [[_COMMUNITY_Radiation Management|Radiation Management]]
- [[_COMMUNITY_Nano Equipment Listener|Nano Equipment Listener]]
- [[_COMMUNITY_Visual Displays and Holograms|Visual Displays and Holograms]]
- [[_COMMUNITY_Pipe Network Logic|Pipe Network Logic]]
- [[_COMMUNITY_Quarry User Interface|Quarry User Interface]]
- [[_COMMUNITY_Reactor User Interface|Reactor User Interface]]
- [[_COMMUNITY_Mass Fabricator UI|Mass Fabricator UI]]
- [[_COMMUNITY_Hologram Display Manager|Hologram Display Manager]]
- [[_COMMUNITY_Guide Menu Interface|Guide Menu Interface]]
- [[_COMMUNITY_WorldGuard Integration Hook|WorldGuard Integration Hook]]

## God Nodes (most connected - your core abstractions)
1. `RecipeRegistry` - 72 edges
2. `MachineDataStorage` - 69 edges
3. `PipeNetwork` - 38 edges
4. `IndustrialManager` - 31 edges
5. `QuarryManager` - 26 edges
6. `QuarryWorker` - 25 edges
7. `NuclearReactorCore` - 24 edges
8. `DeliveryService` - 24 edges
9. `EventManager` - 22 edges
10. `AstraBuildPlugin` - 21 edges

## Surprising Connections (you probably didn't know these)
- `AstraBuildPlugin` --references--> `EventManager`  [EXTRACTED]
  src/main/java/com/astrabuild/AstraBuildPlugin.java → src/main/java/com/astrabuild/events/EventManager.java
- `AstraBuildPlugin` --references--> `RecipeRegistry`  [EXTRACTED]
  src/main/java/com/astrabuild/AstraBuildPlugin.java → src/main/java/com/astrabuild/item/RecipeRegistry.java
- `AstraBuildPlugin` --references--> `DeliveryService`  [EXTRACTED]
  src/main/java/com/astrabuild/AstraBuildPlugin.java → src/main/java/com/astrabuild/network/DeliveryService.java
- `AstraBuildPlugin` --references--> `QuarryManager`  [EXTRACTED]
  src/main/java/com/astrabuild/AstraBuildPlugin.java → src/main/java/com/astrabuild/quarry/QuarryManager.java
- `AstraBuildPlugin` --references--> `RadiationManager`  [EXTRACTED]
  src/main/java/com/astrabuild/AstraBuildPlugin.java → src/main/java/com/astrabuild/strike/RadiationManager.java

## Import Cycles
- None detected.

## Communities (26 total, 0 thin omitted)

### Community 0 - "Core Plugin and Listeners"
Cohesion: 0.06
Nodes (36): BlockBreakEvent, BlockExplodeEvent, BlockPlaceEvent, ChunkLoadEvent, ChunkUnloadEvent, EntityExplodeEvent, JavaPlugin, Listener (+28 more)

### Community 1 - "Events and Quarry Worker"
Cohesion: 0.09
Nodes (22): Collection, Queue, EventManager, JavaPlugin, String, EventType, getDescription(), getTitle() (+14 more)

### Community 2 - "Recipe and GUI Listeners"
Cohesion: 0.09
Nodes (19): Inventory, ItemStack, Override, Player, SuppressWarnings, RecipeViewGui, Block, Inventory (+11 more)

### Community 3 - "Item Recipe Registry"
Cohesion: 0.14
Nodes (7): ItemStack, Material, NamespacedKey, Plugin, String, SuppressWarnings, RecipeRegistry

### Community 4 - "Core GUI Implementations"
Cohesion: 0.11
Nodes (16): InventoryHolder, AdminGui, Inventory, Override, Player, SuppressWarnings, CentrifugeGui, Block (+8 more)

### Community 5 - "Orbital Strike Mechanism"
Cohesion: 0.12
Nodes (17): CoreProtectAPI, EventHandler, NamespacedKey, PlayerInteractEvent, Plugin, SuppressWarnings, OrbitalStrikeListener, Block (+9 more)

### Community 6 - "Centrifuge and Mass Fabricator"
Cohesion: 0.12
Nodes (18): Integer, CentrifugeWorker, Inventory, ItemStack, Location, Random, Inventory, ItemStack (+10 more)

### Community 7 - "Energy Storage and Uranium"
Cohesion: 0.14
Nodes (12): EnergyStorageGui, Block, Inventory, Override, Plugin, ItemStack, Material, NamespacedKey (+4 more)

### Community 8 - "Coolant Tank Components"
Cohesion: 0.15
Nodes (8): CoolantTankGui, Inventory, Override, Player, SuppressWarnings, CoolantTankWorker, Location, NamespacedKey

### Community 9 - "Plugin Commands"
Cohesion: 0.17
Nodes (13): Command, CommandExecutor, CommandSender, AstraBuildCommand, List, Override, String, CraftsGui (+5 more)

### Community 10 - "Radiation Management"
Cohesion: 0.20
Nodes (8): Chunk, ItemStack, Location, NamespacedKey, Player, Plugin, String, RadiationManager

### Community 11 - "Nano Equipment Listener"
Cohesion: 0.26
Nodes (8): EntityDamageByEntityEvent, PlayerJoinEvent, EventHandler, ItemStack, NamespacedKey, Plugin, SuppressWarnings, NanoEquipmentListener

### Community 12 - "Visual Displays and Holograms"
Cohesion: 0.18
Nodes (9): ItemDisplay, Override, DisplayUtil, ItemStack, List, Location, Map, Plugin (+1 more)

### Community 13 - "Pipe Network Logic"
Cohesion: 0.24
Nodes (9): Location, NetworkNode, NodeType, BlockPos, List, Location, Set, PathResult (+1 more)

### Community 14 - "Quarry User Interface"
Cohesion: 0.22
Nodes (7): Block, Inventory, Override, Player, String, SuppressWarnings, QuarryGui

### Community 15 - "Reactor User Interface"
Cohesion: 0.20
Nodes (5): Inventory, Override, Player, SuppressWarnings, ReactorGui

### Community 16 - "Mass Fabricator UI"
Cohesion: 0.31
Nodes (5): Block, Inventory, Override, Plugin, MassFabricatorGui

### Community 17 - "Hologram Display Manager"
Cohesion: 0.27
Nodes (8): HologramManager, Location, Map, NamespacedKey, Plugin, String, SuppressWarnings, TextDisplay

### Community 18 - "Guide Menu Interface"
Cohesion: 0.29
Nodes (6): GuideMenuGui, Inventory, InventoryClickEvent, Override, Player, SuppressWarnings

### Community 19 - "WorldGuard Integration Hook"
Cohesion: 0.60
Nodes (3): Location, Player, WorldGuardHook

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `MachineDataStorage` connect `Core Plugin and Listeners` to `Events and Quarry Worker`, `Recipe and GUI Listeners`, `Item Recipe Registry`, `Core GUI Implementations`, `Centrifuge and Mass Fabricator`, `Energy Storage and Uranium`, `Coolant Tank Components`, `Quarry User Interface`, `Mass Fabricator UI`?**
  _High betweenness centrality (0.249) - this node is a cross-community bridge._
- **Why does `RecipeRegistry` connect `Item Recipe Registry` to `Core Plugin and Listeners`, `Events and Quarry Worker`, `Recipe and GUI Listeners`, `Core GUI Implementations`, `Orbital Strike Mechanism`, `Centrifuge and Mass Fabricator`, `Energy Storage and Uranium`, `Plugin Commands`, `Radiation Management`, `Quarry User Interface`, `Guide Menu Interface`?**
  _High betweenness centrality (0.228) - this node is a cross-community bridge._
- **Why does `PipeNetwork` connect `Core Plugin and Listeners` to `Events and Quarry Worker`, `Item Recipe Registry`, `Pipe Network Logic`, `Centrifuge and Mass Fabricator`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._
- **Should `Core Plugin and Listeners` be split into smaller, more focused modules?**
  _Cohesion score 0.05960729312762973 - nodes in this community are weakly interconnected._
- **Should `Events and Quarry Worker` be split into smaller, more focused modules?**
  _Cohesion score 0.08928571428571429 - nodes in this community are weakly interconnected._
- **Should `Recipe and GUI Listeners` be split into smaller, more focused modules?**
  _Cohesion score 0.08985507246376812 - nodes in this community are weakly interconnected._
- **Should `Item Recipe Registry` be split into smaller, more focused modules?**
  _Cohesion score 0.13732004429678848 - nodes in this community are weakly interconnected._