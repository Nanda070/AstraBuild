package com.astrabuild.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ConnectorInsertGui implements InventoryHolder {

    private final Inventory virtualInventory;
    private final Block machineBlock;
    private final Plugin plugin;
    
    // Карта слотов: индекс массива = слот в реальном инвентаре (0-8), значение = слот в GUI
    private static final int[] SLOT_MAP = {
            3, 4, 5,
            12, 13, 14,
            21, 22, 23
    };

    public ConnectorInsertGui(Block machineBlock, Plugin plugin) {
        this.machineBlock = machineBlock;
        this.plugin = plugin;
        this.virtualInventory = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§8Настройки Коннектора"));
        setupLayout();
        syncFromBlock();
        startSyncTask();
    }

    private void setupLayout() {
        ItemStack bg = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        if (bgMeta != null) {
            bgMeta.displayName(Component.empty());
            bg.setItemMeta(bgMeta);
        }

        for (int i = 0; i < virtualInventory.getSize(); i++) {
            boolean isBufferSlot = false;
            for (int bufferSlot : SLOT_MAP) {
                if (i == bufferSlot) {
                    isBufferSlot = true;
                    break;
                }
            }
            if (!isBufferSlot) {
                virtualInventory.setItem(i, bg);
            }
        }
    }

    public void syncFromBlock() {
        if (machineBlock.getState() instanceof Container container) {
            Inventory realInv = container.getInventory();
            for (int i = 0; i < SLOT_MAP.length; i++) {
                virtualInventory.setItem(SLOT_MAP[i], realInv.getItem(i));
            }
        }
    }

    public void syncToBlock() {
        if (machineBlock.getState() instanceof Container container) {
            Inventory realInv = container.getInventory();
            for (int i = 0; i < SLOT_MAP.length; i++) {
                realInv.setItem(i, virtualInventory.getItem(SLOT_MAP[i]));
            }
        }
    }

    private void startSyncTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (virtualInventory.getViewers().isEmpty()) {
                    this.cancel();
                    return;
                }
                syncFromBlock();
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    @Override
    public Inventory getInventory() {
        return virtualInventory;
    }
}
