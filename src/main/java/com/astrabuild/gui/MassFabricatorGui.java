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

public class MassFabricatorGui implements InventoryHolder {

    private final Inventory virtualInventory;
    private final Block machineBlock;
    private final MachineDataStorage storage;
    private final Plugin plugin;

    public MassFabricatorGui(Block machineBlock, MachineDataStorage storage, Plugin plugin) {
        this.machineBlock = machineBlock;
        this.storage = storage;
        this.plugin = plugin;
        this.virtualInventory = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§dГенератор Материи"));
        setupLayout();
        syncFromBlock();
        startSyncTask();
    }

    private void setupLayout() {
        ItemStack bg = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        if (bgMeta != null) {
            bgMeta.displayName(Component.empty());
            bg.setItemMeta(bgMeta);
        }

        for (int i = 0; i < virtualInventory.getSize(); i++) {
            virtualInventory.setItem(i, bg);
        }

        // 11 - Input (Scrap)
        // 15 - Output (Diamond/Matter)
        virtualInventory.setItem(11, new ItemStack(Material.AIR));
        virtualInventory.setItem(15, new ItemStack(Material.AIR));
        
        // Info Item at slot 13
        updateInfoItem();
    }

    public void updateInfoItem() {
        ItemStack info = new ItemStack(Material.DIAMOND);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize("Â§dÐ¡Ñ‚Ð°Ñ‚ÑƒÑ: Ð“ÐµÐ½ÐµÑ€Ð°Ñ‚Ð¾Ñ€ ÐœÐ°Ñ‚ÐµÑ€Ð¸Ð¸"));
            int energy = storage.getEnergy(machineBlock);
            meta.lore(List.of(
                    LegacyComponentSerializer.legacySection().deserialize("Â§7Ð­Ð½ÐµÑ€Ð³Ð¸Ñ: Â§a" + energy + " Â§7/ Â§a" + storage.getMaxEnergy("MASS_FABRICATOR") + " EU"),
                    LegacyComponentSerializer.legacySection().deserialize("Â§7ÐŸÐ¾Ñ‚Ñ€ÐµÐ±Ð»ÐµÐ½Ð¸Ðµ: Â§c1,000,000 EU/Ð°Ð»Ð¼Ð°Ð·"),
                    LegacyComponentSerializer.legacySection().deserialize(""),
                    LegacyComponentSerializer.legacySection().deserialize("Â§fÐ’Ñ…Ð¾Ð´: Ð£Ñ‚Ð¸Ð»ÑŒ (Ð—ÐµÐ¼Ð»Ñ, Ð±ÑƒÐ»Ñ‹Ð¶Ð½Ð¸Ðº, Ð¿ÐµÑÐ¾Ðº)"),
                    LegacyComponentSerializer.legacySection().deserialize("Â§fÐ’Ñ‹Ñ…Ð¾Ð´: ÐÐ»Ð¼Ð°Ð·Ñ‹")
            ));
            info.setItemMeta(meta);
        }
        virtualInventory.setItem(13, info);
    }

    public void syncFromBlock() {
        if (machineBlock.getState() instanceof Container container) {
            Inventory realInv = container.getInventory();
            virtualInventory.setItem(11, realInv.getItem(0));
            virtualInventory.setItem(15, realInv.getItem(1));
        }
    }

    public void syncToBlock() {
        if (machineBlock.getState() instanceof Container container) {
            Inventory realInv = container.getInventory();
            realInv.setItem(0, virtualInventory.getItem(11));
            realInv.setItem(1, virtualInventory.getItem(15));
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
