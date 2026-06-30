package com.astrabuild.machine;

import com.astrabuild.item.RecipeRegistry;
import com.astrabuild.network.DeliveryService;
import com.astrabuild.network.PipeNetwork;
import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndustrialManager {
    private final Plugin plugin;
    private final MachineDataStorage storage;
    private final DeliveryService deliveryService;
    private final PipeNetwork network;
    private final RecipeRegistry recipeRegistry;

    private final Map<Location, NuclearReactorCore> reactors = new ConcurrentHashMap<>();
    private final Map<Location, CentrifugeWorker> centrifuges = new ConcurrentHashMap<>();
    private final Map<Location, MassFabricatorWorker> fabricators = new ConcurrentHashMap<>();
    private final Map<Location, EnergyStorageWorker> energyStorages = new ConcurrentHashMap<>();
    private final Map<Location, CoolantTankWorker> coolantTanks = new ConcurrentHashMap<>();

    private final BukkitTask tickerTask;

    public IndustrialManager(Plugin plugin, MachineDataStorage storage, DeliveryService deliveryService, PipeNetwork network, RecipeRegistry recipeRegistry) {
        this.plugin = plugin;
        this.storage = storage;
        this.deliveryService = deliveryService;
        this.network = network;
        this.recipeRegistry = recipeRegistry;

        this.tickerTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickAll, 20L, 1L);
    }

    public void registerMachine(Location loc, String type) {
        switch (type) {
            case "REACTOR_CORE" -> reactors.put(loc, new NuclearReactorCore(plugin, loc, storage, network, this));
            case "CENTRIFUGE" -> centrifuges.put(loc, new CentrifugeWorker(loc, storage, deliveryService, network, recipeRegistry));
            case "MASS_FABRICATOR" -> fabricators.put(loc, new MassFabricatorWorker(loc, storage, deliveryService, network));
            case "ENERGY_STORAGE" -> energyStorages.put(loc, new EnergyStorageWorker(loc, storage));
            case "COOLANT_TANK" -> coolantTanks.put(loc, new CoolantTankWorker(loc, storage));
        }
    }

    public void unregisterMachine(Location loc) {
        reactors.remove(loc);
        centrifuges.remove(loc);
        fabricators.remove(loc);
        energyStorages.remove(loc);
        coolantTanks.remove(loc);
    }

    private void tickAll() {
        reactors.forEach((loc, r) -> { if (r != null && isLoaded(loc)) { if (storage.getMachineType(loc.getBlock()) != null) r.runTick(); else cleanupDeadMachine(loc); } });
        centrifuges.forEach((loc, c) -> { if (c != null && isLoaded(loc)) { if (storage.getMachineType(loc.getBlock()) != null) c.runTick(); else cleanupDeadMachine(loc); } });
        fabricators.forEach((loc, f) -> { if (f != null && isLoaded(loc)) { if (storage.getMachineType(loc.getBlock()) != null) f.runTick(); else cleanupDeadMachine(loc); } });
        energyStorages.forEach((loc, e) -> { if (e != null && isLoaded(loc)) { if (storage.getMachineType(loc.getBlock()) != null) e.runTick(); else cleanupDeadMachine(loc); } });
        coolantTanks.forEach((loc, t) -> { if (t != null && isLoaded(loc)) { if (storage.getMachineType(loc.getBlock()) != null) t.runTick(); else cleanupDeadMachine(loc); } });
    }

    private void cleanupDeadMachine(Location loc) {
        org.bukkit.block.Block block = loc.getBlock();
        if (block.getState() instanceof org.bukkit.block.Container container) {
            for (org.bukkit.inventory.ItemStack invItem : container.getInventory().getContents()) {
                if (invItem != null && !invItem.getType().isAir()) {
                    loc.getWorld().dropItemNaturally(loc, invItem);
                }
            }
            container.getInventory().clear();
        }
        unregisterMachine(loc);
        network.unregisterNode(loc);
        com.astrabuild.AstraBuildPlugin.getInstance().getHologramManager().removeHologram(loc);
    }

    private boolean isLoaded(Location loc) {
        return loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public Map<Location, CoolantTankWorker> getCoolantTanks() {
        return coolantTanks;
    }

    public Map<Location, NuclearReactorCore> getReactors() {
        return reactors;
    }

    public void shutdown() {
        if (tickerTask != null) tickerTask.cancel();
        reactors.clear();
        centrifuges.clear();
        fabricators.clear();
        energyStorages.clear();
        coolantTanks.clear();
    }
}

