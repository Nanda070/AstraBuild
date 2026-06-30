package com.astrabuild.gui;

import com.astrabuild.storage.MachineDataStorage;
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

import java.util.List;

public class EnergyStorageGui implements InventoryHolder {

    private final Inventory virtualInventory;
    private final Block machineBlock;
    private final MachineDataStorage storage;
    private final Plugin plugin;
    
    private final int[] batterySlots = {23, 14, 5}; // 3 glass panes acting as battery levels (bottom to top)

    public EnergyStorageGui(Block machineBlock, MachineDataStorage storage, Plugin plugin) {
        this.machineBlock = machineBlock;
        this.storage = storage;
        this.plugin = plugin;
        this.virtualInventory = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§aМФСУ"));
        setupLayout();
        syncFromBlock();
        startSyncTask();
    }

    private void setupLayout() {
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        if (bgMeta != null) {
            bgMeta.displayName(Component.empty());
            bg.setItemMeta(bgMeta);
        }

        for (int i = 0; i < virtualInventory.getSize(); i++) {
            virtualInventory.setItem(i, bg);
        }

        // 11 - Input (For charging nano equipment)
        virtualInventory.setItem(11, new ItemStack(Material.AIR));
        
        // Info Item at slot 13
        updateInfoItem();
    }

    public void updateInfoItem() {
        ItemStack info = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta meta = info.getItemMeta();
        int energy = storage.getEnergy(machineBlock);
        int max = storage.getMaxEnergy("ENERGY_STORAGE");
        
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize("§aСтатус МФСУ"));
            meta.lore(List.of(
                    LegacyComponentSerializer.legacySection().deserialize("§7Запас энергии: §a" + energy + " §7/ §a" + max + " EU"),
                    LegacyComponentSerializer.legacySection().deserialize(""),
                    LegacyComponentSerializer.legacySection().deserialize("§fВход: Труба (в любой блок)"),
                    LegacyComponentSerializer.legacySection().deserialize("§fВыход: Труба (из любого блока)")
            ));
            info.setItemMeta(meta);
        }
        virtualInventory.setItem(13, info);
        
        // Обновляем визуальную батарею
        double percentage = (double) energy / max;
        int activeBars = (int) Math.round(percentage * 3); // 0, 1, 2, 3
        
        ItemStack greenPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta greenMeta = greenPane.getItemMeta();
        
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        
        String chargeText = "§aЗаряжается";
        String emptyText = "§cРазряжается";
        
        ItemStack chargingItem = virtualInventory.getItem(11);
        if (chargingItem != null && chargingItem.hasItemMeta()) {
            org.bukkit.persistence.PersistentDataContainer pdc = chargingItem.getItemMeta().getPersistentDataContainer();
            org.bukkit.NamespacedKey energyKey = new org.bukkit.NamespacedKey(plugin, "astrabuild_energy");
            if (pdc.has(energyKey, org.bukkit.persistence.PersistentDataType.INTEGER)) {
                int itemEnergy = pdc.getOrDefault(energyKey, org.bukkit.persistence.PersistentDataType.INTEGER, 0);
                
                String handType = com.astrabuild.AstraBuildPlugin.getInstance().getRecipeRegistry().getMachineItemType(chargingItem);
                int maxItemEnergy = 0;
                if ("NANO_CHESTPLATE".equals(handType)) maxItemEnergy = 10000;
                else if ("NANO_SWORD".equals(handType)) maxItemEnergy = 5000;
                
                if (maxItemEnergy > 0) {
                    chargeText = "§aЗаряжается: " + itemEnergy + " / " + maxItemEnergy + " EU";
                    emptyText = "§cЗарядка: " + itemEnergy + " / " + maxItemEnergy + " EU";
                }
            }
        }
        
        if (greenMeta != null) {
            greenMeta.displayName(LegacyComponentSerializer.legacySection().deserialize(chargeText));
            greenPane.setItemMeta(greenMeta);
        }
        
        if (redMeta != null) {
            redMeta.displayName(LegacyComponentSerializer.legacySection().deserialize(emptyText));
            redPane.setItemMeta(redMeta);
        }

        for (int i = 0; i < 3; i++) {
            if (i < activeBars) {
                virtualInventory.setItem(batterySlots[i], greenPane);
            } else {
                virtualInventory.setItem(batterySlots[i], redPane);
            }
        }
    }

    public void syncFromBlock() {
        if (machineBlock.getState() instanceof Container container) {
            Inventory realInv = container.getInventory();
            virtualInventory.setItem(11, realInv.getItem(0));
        }
    }

    public void syncToBlock() {
        if (machineBlock.getState() instanceof Container container) {
            Inventory realInv = container.getInventory();
            realInv.setItem(0, virtualInventory.getItem(11));
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
                updateInfoItem();
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    @Override
    public Inventory getInventory() {
        return virtualInventory;
    }
}
