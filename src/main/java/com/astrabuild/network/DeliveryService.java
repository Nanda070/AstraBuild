package com.astrabuild.network;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public class DeliveryService {

    private final Plugin plugin;

    public DeliveryService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void deliverItem(Location destination, ItemStack item) {
        if (item == null || item.getType().isAir()) return;

        Block connectorBlock = destination.getBlock();
        BlockState state = connectorBlock.getState();

        // Читаем куда повернут коннектор (Dropper) и бьем напрямую в этот блок
        if (state.getBlockData() instanceof Directional directional) {
            Block targetBlock = connectorBlock.getRelative(directional.getFacing());
            
            if (targetBlock.getState() instanceof Container chest) {
                Map<Integer, ItemStack> leftovers = chest.getInventory().addItem(item);
                if (!leftovers.isEmpty()) dropExcess(destination, leftovers);
                return;
            }
        }

        // Запасной вариант - если сундука нет спереди, кидаем внутрь самого коннектора
        if (state instanceof Container container) {
            Map<Integer, ItemStack> leftovers = container.getInventory().addItem(item);
            if (!leftovers.isEmpty()) dropExcess(destination, leftovers);
            return;
        }

        // Штрафной сброс (система перегружена или сломана)
        dropExcess(destination, Map.of(0, item));
    }

    private void dropExcess(Location loc, Map<Integer, ItemStack> items) {
        Location dropLoc = loc.clone().add(0.5, 1.0, 0.5);
        for (ItemStack leftover : items.values()) {
            dropLoc.getWorld().dropItemNaturally(dropLoc, leftover);
        }
    }
}