package com.astrabuild.storage;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class MachineDataStorage {

    private final NamespacedKey machineTypeKey;
    private final NamespacedKey boundsKey;
    private final NamespacedKey energyKey;

    public MachineDataStorage(Plugin plugin) {
        this.machineTypeKey = new NamespacedKey(plugin, "machine_type");
        this.boundsKey = new NamespacedKey(plugin, "quarry_bounds");
        this.energyKey = new NamespacedKey(plugin, "machine_energy");
    }

    /**
     * Помечает блок как механизм AstraBuild
     */
    public void setMachineType(Block block, String type) {
        if (block.getState() instanceof TileState state) {
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            pdc.set(machineTypeKey, PersistentDataType.STRING, type);
            state.update();
        }
    }

    public Location[] getQuarryBoundsParsed(Block block) {
        if (block.getState() instanceof TileState state) {
            String data = state.getPersistentDataContainer().get(boundsKey, PersistentDataType.STRING);
            if (data == null) return null;
            
            try {
                String[] parts = data.split(";");
                String[] p1Str = parts[0].split(",");
                String[] p2Str = parts[1].split(",");
                
                Location p1 = new Location(block.getWorld(), Integer.parseInt(p1Str[0]), Integer.parseInt(p1Str[1]), Integer.parseInt(p1Str[2]));
                Location p2 = new Location(block.getWorld(), Integer.parseInt(p2Str[0]), Integer.parseInt(p2Str[1]), Integer.parseInt(p2Str[2]));
                return new Location[]{p1, p2};
            } catch (Exception e) {
                return null; // Защита от поврежденных данных
            }
        }
        return null;
    }

    public int getEnergy(Block block) {
        if (block.getState() instanceof TileState state) {
            return state.getPersistentDataContainer().getOrDefault(energyKey, PersistentDataType.INTEGER, 0);
        }
        return 0;
    }

    public void addEnergy(Block block, int amount) {
        if (block.getState() instanceof TileState state) {
            int current = getEnergy(block);
            state.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, current + amount);
            state.update();
        }
    }

    public boolean consumeEnergy(Block block, int amount) {
        if (block.getState() instanceof TileState state) {
            int current = getEnergy(block);
            if (current >= amount) {
                state.getPersistentDataContainer().set(energyKey, PersistentDataType.INTEGER, current - amount);
                state.update();
                return true;
            }
        }
        return false;
    }

    public void setQuarryBounds(Block block, Location p1, Location p2) {
        if (block.getState() instanceof TileState state) {
            String boundsData = String.format("%d,%d,%d;%d,%d,%d",
                    p1.getBlockX(), p1.getBlockY(), p1.getBlockZ(),
                    p2.getBlockX(), p2.getBlockY(), p2.getBlockZ());
            
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            pdc.set(boundsKey, PersistentDataType.STRING, boundsData);
            state.update();
        }
    }

    /**
     * Читает тип механизма из блока
     */
    public String getMachineType(Block block) {
        if (block.getState() instanceof TileState state) {
            return state.getPersistentDataContainer().get(machineTypeKey, PersistentDataType.STRING);
        }
        return null;
    }

    /**
     * Очищает данные блока при разрушении
     */
    public void clearMachineData(Block block) {
        if (block.getState() instanceof TileState state) {
            PersistentDataContainer pdc = state.getPersistentDataContainer();
            pdc.remove(machineTypeKey);
            pdc.remove(boundsKey);
            state.update();
        }
    }
}