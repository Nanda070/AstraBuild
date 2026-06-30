package com.astrabuild.quarry;

import com.astrabuild.events.EventManager;
import com.astrabuild.events.EventType;
import com.astrabuild.network.DeliveryService;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.storage.MachineDataStorage;
import com.astrabuild.visual.DisplayUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public class QuarryWorker {
    private final Plugin plugin;
    private final Location controllerLoc;
    private final PipeNetwork network;
    private final DeliveryService deliveryService;
    private final MachineDataStorage storage;
    private final EventManager eventManager;
    private final Queue<Location> scanQueue = new LinkedList<>();
    
    private boolean active = true;
    private int cooldown = 0; // Задержка копки
    private final ItemStack fakePickaxe = new ItemStack(Material.DIAMOND_PICKAXE);

    private static final Set<Material> DEEP_SCAN_JUNK = EnumSet.of(
            Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.GRAVEL,
            Material.ANDESITE, Material.DIORITE, Material.GRANITE
    );

    private boolean hasFortune;
    private boolean hasSilkTouch;
    private boolean hasOverclocker;
    private final Random random = new Random();

    private final Set<org.bukkit.Chunk> loadedChunks = new HashSet<>();
    private boolean chunksAreLoaded = false;

    public QuarryWorker(Plugin plugin, Location controllerLoc, PipeNetwork network, DeliveryService deliveryService, MachineDataStorage storage, EventManager eventManager, Location p1, Location p2) {
        this.plugin = plugin;
        this.controllerLoc = controllerLoc;
        this.network = network;
        this.deliveryService = deliveryService;
        this.storage = storage;
        this.eventManager = eventManager;
        initializeQueue(p1, p2);
        loadUpgrades();
        addChunkTickets();
    }

    private void initializeQueue(Location p1, Location p2) {
        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());
        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());
        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    scanQueue.add(new Location(p1.getWorld(), x, y, z));
                }
            }
        }

        // Collect chunks for Chunk Loading
        org.bukkit.World world = p1.getWorld();
        int minCX = minX >> 4;
        int maxCX = maxX >> 4;
        int minCZ = minZ >> 4;
        int maxCZ = maxZ >> 4;
        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                loadedChunks.add(world.getChunkAt(cx, cz));
            }
        }
        loadedChunks.add(controllerLoc.getChunk());
    }

    private void loadUpgrades() {
        this.hasFortune = storage.hasUpgrade(controllerLoc.getBlock(), "UPGRADE_FORTUNE");
        this.hasSilkTouch = storage.hasUpgrade(controllerLoc.getBlock(), "UPGRADE_SILK_TOUCH");
        this.hasOverclocker = storage.hasUpgrade(controllerLoc.getBlock(), "UPGRADE_OVERCLOCKER");
    }

    private void addChunkTickets() {
        if (chunksAreLoaded) return;
        for (org.bukkit.Chunk chunk : loadedChunks) {
            chunk.addPluginChunkTicket(plugin);
        }
        chunksAreLoaded = true;
    }

    private void removeChunkTickets() {
        if (!chunksAreLoaded) return;
        for (org.bukkit.Chunk chunk : loadedChunks) {
            chunk.removePluginChunkTicket(plugin);
        }
        chunksAreLoaded = false;
    }

    public boolean isActive() {
        return this.active;
    }

    public void runTick() {
        if (!active) return;
        
        loadUpgrades();

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        Location targetLoc = null;
        Block block = null;

        boolean isDeepScan = eventManager != null && eventManager.getCurrentEvent() == EventType.DEEP_SCAN;

        // Мгновенный пропуск воздуха, бедрока и мусора (если DEEP_SCAN) без затрат тиков
        while (!scanQueue.isEmpty()) {
            targetLoc = scanQueue.peek();
            block = targetLoc.getBlock();
            Material type = block.getType();
            
            if (type.isAir() || type == Material.BEDROCK || (isDeepScan && DEEP_SCAN_JUNK.contains(type))) {
                scanQueue.poll();
                if (isDeepScan && DEEP_SCAN_JUNK.contains(type)) {
                    block.setType(Material.AIR, false); // Уничтожаем мусор
                }
                block = null;
            } else {
                break;
            }
        }

        if (targetLoc == null || block == null) {
            active = false;
            removeChunkTickets();
            return;
        }

        int energyCost = 1;
        if (hasOverclocker) {
            energyCost = 3;
        }
        if (eventManager != null && eventManager.getCurrentEvent() == EventType.HEAVY_ROCK) {
            energyCost *= 2;
        }

        if (!storage.consumeEnergy(controllerLoc.getBlock(), energyCost)) {
            removeChunkTickets();
            return;
        } else {
            addChunkTickets();
        }

        scanQueue.poll();
        Collection<ItemStack> drops = new LinkedList<>();
        
        if (hasSilkTouch) {
            drops.add(new ItemStack(block.getType()));
        } else {
            Collection<ItemStack> normalDrops = block.getDrops(fakePickaxe);
            if (hasFortune) {
                for (ItemStack drop : normalDrops) {
                    if (isOre(block.getType()) && !block.hasMetadata("astrabuild_player_placed")) {
                        int multiplier = 1 + random.nextInt(3); // x1, x2, or x3
                        drop.setAmount(drop.getAmount() * multiplier);
                    }
                    drops.add(drop);
                }
            } else {
                drops.addAll(normalDrops);
            }
        }
        
        // Визуал ломания блока
        targetLoc.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, targetLoc.clone().add(0.5, 0.5, 0.5), 15, block.getBlockData());
        block.setType(Material.AIR, true);
            
        if (!drops.isEmpty()) {
            pushToNetwork(drops);
        }

        if (hasOverclocker) {
            this.cooldown = 3; 
        } else {
            this.cooldown = (eventManager != null && eventManager.getCurrentEvent() == EventType.ACTIVE_MINING) ? 5 : 6; 
        }
    }

    private boolean isOre(Material material) {
        return material.name().endsWith("_ORE");
    }

    private void pushToNetwork(Collection<ItemStack> items) {
        PipeNetwork.PathResult pathResult = network.findShortestPath(controllerLoc);
        
        if (pathResult == null || pathResult.path().isEmpty()) {
            Location dropLoc = controllerLoc.clone().add(0, 1, 0);
            for (ItemStack item : items) {
                dropLoc.getWorld().dropItemNaturally(dropLoc, item);
            }
            return;
        }

        int ticksPerPipe = (eventManager != null && eventManager.getCurrentEvent() == EventType.LOGISTICS_BOOST) ? 2 : 5;

        for (ItemStack item : items) {
            DisplayUtil.animateItemPath(plugin, item, pathResult.path(), ticksPerPipe, () -> {
                deliveryService.deliverItem(pathResult.destination(), item);
            });
        }
    }

    public void halt() {
        this.active = false;
        removeChunkTickets();
    }
}