package com.astrabuild.machine;

import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.block.TileState;
import org.bukkit.NamespacedKey;

public class CoolantTankWorker {

    private final Location tankLocation;

    public static final int MAX_COOLANT = 100000; 
    private int coolantMb = 0;
    private final NamespacedKey coolantKey;

    public CoolantTankWorker(Location tankLocation, MachineDataStorage storage) {
        this.tankLocation = tankLocation;
        this.coolantKey = new NamespacedKey(com.astrabuild.AstraBuildPlugin.getInstance(), "astrabuild_coolant");
        
        Block block = tankLocation.getBlock();
        if (block.getState() instanceof TileState state) {
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            if (pdc.has(coolantKey, PersistentDataType.INTEGER)) {
                this.coolantMb = pdc.getOrDefault(coolantKey, PersistentDataType.INTEGER, 0);
            }
        }
    }
    
    private void saveCoolant() {
        Block block = tankLocation.getBlock();
        if (block.getState() instanceof TileState state) {
            state.getPersistentDataContainer().set(coolantKey, PersistentDataType.INTEGER, this.coolantMb);
            state.update();
        }
    }

    private int tickCounter = 0;

    public void runTick() {
        if (coolantMb < MAX_COOLANT) {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                absorbWaterFromNeighbors();
            }
        }
    }

    private void absorbWaterFromNeighbors() {
        int absorbed = 0;
        Block center = tankLocation.getBlock();

        int[][] directions = {{1,0,0}, {-1,0,0}, {0,1,0}, {0,-1,0}, {0,0,1}, {0,0,-1}};
        for (int[] dir : directions) {
            Block neighbor = center.getRelative(dir[0], dir[1], dir[2]);
            Material type = neighbor.getType();
            
            if (type == Material.WATER) {
                if (neighbor.getBlockData() instanceof Levelled levelled) {
                    if (levelled.getLevel() == 0) {
                        absorbed += 200; 
                    }
                }
            } else if (type == Material.WATER_CAULDRON) {
                if (neighbor.getBlockData() instanceof Levelled levelled) {
                    int level = levelled.getLevel();
                    if (level > 0) {
                        absorbed += 333; 
                        levelled.setLevel(level - 1);
                        neighbor.setBlockData(levelled);
                        if (level - 1 == 0) {
                            neighbor.setType(Material.CAULDRON);
                        }
                    }
                }
            }
            
            if (coolantMb + absorbed >= MAX_COOLANT) {
                break;
            }
        }
        
        if (absorbed > 0) {
            addCoolant(absorbed);
        }
    }

    public int getCoolantMb() {
        return coolantMb;
    }

    public void addCoolant(int amount) {
        this.coolantMb += amount;
        if (this.coolantMb > MAX_COOLANT) {
            this.coolantMb = MAX_COOLANT;
        }
        saveCoolant();
    }
    
    public int consumeCoolant(int amount) {
        int actual = amount;
        if (this.coolantMb < amount) {
            actual = this.coolantMb;
        }
        this.coolantMb -= actual;
        saveCoolant();
        return actual;
    }
}