package com.astrabuild.machine;

import com.astrabuild.strike.OrbitalStrikeManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import com.astrabuild.storage.MachineDataStorage;
import com.astrabuild.network.PipeNetwork;

public class NuclearReactorCore {

    private final Plugin plugin;
    private final Location coreLocation;
    private final MachineDataStorage storage;
    private final PipeNetwork network;
    private final IndustrialManager manager;
    
    private int heat = 0; // 0 to 10000
    private int warningTicks = 0;
    private int coolantMb = 0;
    
    private static final int HEAT_PER_TICK = 5;
    private static final int ENERGY_PER_TICK = 1000;
    public static final int MAX_HEAT = 10000;
    private static final int MAX_COOLANT_BUFFER = 1000;

    public NuclearReactorCore(Plugin plugin, Location coreLocation, MachineDataStorage storage, PipeNetwork network, IndustrialManager manager) {
        this.plugin = plugin;
        this.coreLocation = coreLocation;
        this.storage = storage;
        this.network = network;
        this.manager = manager;
    }

    public boolean isAssembled() {
        return isStructureIntact();
    }

    public int getHeat() {
        return heat;
    }

    public int getGeneratedEnergy() {
        return ENERGY_PER_TICK;
    }

    public int getCoolantMb() {
        return coolantMb;
    }

    private boolean isStructureIntact() {
        org.bukkit.World world = coreLocation.getWorld();
        if (world == null) return false;
        int cx = coreLocation.getBlockX();
        int cy = coreLocation.getBlockY();
        int cz = coreLocation.getBlockZ();
        
        int ironCount = 0;

        for (int y = -1; y <= 0; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Core
                    
                    org.bukkit.block.Block b = world.getBlockAt(cx + x, cy + y, cz + z);
                    if (y == -1) {
                        // Bottom layer should be iron blocks
                        if (b.getType() == org.bukkit.Material.IRON_BLOCK) ironCount++;
                    } else {
                        // Middle layer corners
                        if (Math.abs(x) == 1 && Math.abs(z) == 1) {
                            if (b.getType() == org.bukkit.Material.IRON_BLOCK) ironCount++;
                        }
                    }
                }
            }
        }
        
        return ironCount >= 13;
    }

    private int tickCounter = 0;

    public void runTick() {
        if (!isStructureIntact()) {
            if (heat > 0) heat -= 5;
            if (heat < 0) heat = 0;
            updateHologram(false);
            return;
        }

        tickCounter++;
        updateHologram(true);

        storage.addEnergy(coreLocation.getBlock(), ENERGY_PER_TICK);

        if (tickCounter >= 20) {
            tickCounter = 0;
            
            int totalGenerated = storage.getEnergy(coreLocation.getBlock());
            java.util.Set<Location> connectedNodes = network.getConnectedNodes(coreLocation, com.astrabuild.model.NetworkNode.NodeType.PIPE_ENERGY);
            
            for (Location loc : connectedNodes) {
                if (totalGenerated <= 0) break;
                org.bukkit.block.Block targetBlock = loc.getBlock();
                String targetType = storage.getMachineType(targetBlock);
                if (targetType != null && !targetType.equals("REACTOR_CORE")) {
                    int currentEnergy = storage.getEnergy(targetBlock);
                    int maxEnergy = storage.getMaxEnergy(targetType);
                    int canTake = maxEnergy - currentEnergy;
                    if (canTake > 0) {
                        int toGive = Math.min(totalGenerated, canTake);
                        storage.addEnergy(targetBlock, toGive);
                        totalGenerated -= toGive;
                    }
                }
            }
            
            if (storage.getEnergy(coreLocation.getBlock()) != totalGenerated) {
                storage.consumeEnergy(coreLocation.getBlock(), storage.getEnergy(coreLocation.getBlock()) - totalGenerated);
            }

            pullCoolantFromNetwork();
            updatePassiveCooling();
        }

        int cooling = passiveCooling;
        if (heat > 0 && coolantMb > 0) {
            int canCool = Math.min(20, coolantMb);
            coolantMb -= canCool;
            cooling += canCool;
        }
        
        heat += HEAT_PER_TICK;
        heat -= cooling;
        
        if (heat < 0) heat = 0;

        if (heat >= MAX_HEAT) {
            triggerMeltdown();
        } else if (heat >= MAX_HEAT * 0.8) {
            if (warningTicks % 20 == 0) {
                coreLocation.getWorld().playSound(coreLocation, Sound.BLOCK_BEACON_ACTIVATE, 5.0f, 0.1f);
                coreLocation.getWorld().playSound(coreLocation, Sound.ENTITY_ENDER_DRAGON_GROWL, 5.0f, 0.1f);
            }
            warningTicks++;
        } else {
            warningTicks = 0;
        }
    }

    private void updateHologram(boolean isIntact) {
        if (tickCounter % 20 != 0 && isIntact) return; 
        com.astrabuild.visual.HologramManager hm = com.astrabuild.AstraBuildPlugin.getInstance().getHologramManager();
        if (hm == null) return;
        
        if (!isIntact) {
            hm.updateHologram(coreLocation, "§c[РЕАКТОР СЛОМАН]\n§7Нужна база 3x3\n§7и 4 железных столба");
            return;
        }

        int energy = storage.getEnergy(coreLocation.getBlock());
        int max = storage.getMaxEnergy("REACTOR_CORE");
        String heatColor = heat < 3000 ? "§a" : heat < 7000 ? "§e" : "§c";
        
        String text = "§b⚡ Ядерный Реактор\n" + heatColor + "🔥 Температура: " + heat + " / 10000\n" + "§e⚡ Энергия: " + energy + " / " + max;
        hm.updateHologram(coreLocation, text);
    }

    private void pullCoolantFromNetwork() {
        if (coolantMb >= MAX_COOLANT_BUFFER) return;
        
        for (Location connected : network.getConnectedNodes(coreLocation, com.astrabuild.model.NetworkNode.NodeType.PIPE_ENERGY)) {
            CoolantTankWorker tank = manager.getCoolantTanks().get(connected);
            if (tank != null) {
                int needed = MAX_COOLANT_BUFFER - coolantMb;
                int extracted = tank.consumeCoolant(needed);
                coolantMb += extracted;
                if (coolantMb >= MAX_COOLANT_BUFFER) break;
            }
        }
    }

    private int passiveCooling = 0;

    private void updatePassiveCooling() {
        passiveCooling = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block b = coreLocation.clone().add(x, y, z).getBlock();
                    if (b.getType() == Material.WATER) passiveCooling += 2;
                    if (b.getType() == Material.BLUE_ICE) passiveCooling += 5;
                }
            }
        }
    }

    private void triggerMeltdown() {
        storage.clearMachineData(coreLocation.getBlock());
        manager.unregisterMachine(coreLocation);
        network.unregisterNode(coreLocation);

        com.astrabuild.visual.HologramManager hm = com.astrabuild.AstraBuildPlugin.getInstance().getHologramManager();
        if (hm != null) hm.removeHologram(coreLocation);

        coreLocation.getBlock().setType(Material.AIR);
        
        OrbitalStrikeManager strikeManager = new OrbitalStrikeManager(plugin);
        com.astrabuild.strike.RadiationManager rm = com.astrabuild.AstraBuildPlugin.getInstance().getRadiationManager();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                rm.contaminateChunk(coreLocation.clone().add(x * 16, 0, z * 16).getChunk(), 86400000L); // 24 hours
            }
        }
        
        strikeManager.initiateStrike(null, coreLocation.getBlock(), "REACTOR_MELTDOWN");
    }

    public Location getLocation() {
        return coreLocation;
    }
}