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

public class CentrifugeGui implements InventoryHolder {

    private final Inventory virtualInventory;
    private final Block machineBlock;
    private final MachineDataStorage storage;
    private final Plugin plugin;

    public CentrifugeGui(Block machineBlock, MachineDataStorage storage, Plugin plugin) {
        this.machineBlock = machineBlock;
        this.storage = storage;
        this.plugin = plugin;
        this.virtualInventory = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§eЦентрифуга"));
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

        // 10 - Input
        // 15 - Output 1 (Isotope)
        // 16 - Output 2 (Waste)
        virtualInventory.setItem(10, new ItemStack(Material.AIR));
        virtualInventory.setItem(15, new ItemStack(Material.AIR));
        virtualInventory.setItem(16, new ItemStack(Material.AIR));
        
        // Info Item at slot 13
        updateInfoItem();
    }

    public void updateInfoItem() {
        ItemStack info = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize("§eСтатус: Центрифуга"));
            int energy = storage.getEnergy(machineBlock);
            meta.lore(List.of(
                    LegacyComponentSerializer.legacySection().deserialize("§7Запас энергии: §a" + energy + " §7/ §a" + storage.getMaxEnergy("CENTRIFUGE") + " EU"),
                    LegacyComponentSerializer.legacySection().deserialize("§7Потребление: §c500 EU/операция"),
                    LegacyComponentSerializer.legacySection().deserialize(""),
                    LegacyComponentSerializer.legacySection().deserialize("§fВход: Руда (Необр. Уран)"),
                    LegacyComponentSerializer.legacySection().deserialize("§fВыход: Обогащ. Уран и Изотопы")
            ));
            info.setItemMeta(meta);
        }
        virtualInventory.setItem(13, info);
    }

    public void syncFromBlock() {
        if (machineBlock.getState() instanceof Container container) {
            Inventory realInv = container.getInventory();
            virtualInventory.setItem(10, realInv.getItem(0));
            virtualInventory.setItem(15, realInv.getItem(1));
            virtualInventory.setItem(16, realInv.getItem(2));
        }
    }

    public void syncToBlock() {
        if (machineBlock.getState() instanceof Container container) {
            Inventory realInv = container.getInventory();
            realInv.setItem(0, virtualInventory.getItem(10));
            realInv.setItem(1, virtualInventory.getItem(15));
            realInv.setItem(2, virtualInventory.getItem(16));
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
        }.runTaskTimer(plugin, 0L, 10L); // Update every half a second
    }

    @Override
    public Inventory getInventory() {
        return virtualInventory;
    }
}
