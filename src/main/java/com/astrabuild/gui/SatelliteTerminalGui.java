package com.astrabuild.gui;

import com.astrabuild.storage.MachineDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@SuppressWarnings("deprecation")
public class SatelliteTerminalGui implements InventoryHolder {

    private final Inventory inventory;
    private final Block terminalBlock;
    private final MachineDataStorage storage;

    public static final int MAX_ENERGY = 100000;

    public SatelliteTerminalGui(Block terminalBlock, MachineDataStorage storage) {
        this.terminalBlock = terminalBlock;
        this.storage = storage;
        this.inventory = Bukkit.createInventory(this, 27, "§dСпутниковый Терминал");
        setupLayout();
    }

    private void setupLayout() {
        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, bg);
        }

        // Слот 11 - Для ввода топлива
        inventory.setItem(11, new ItemStack(Material.AIR));

        // Слот 15 - Кнопка получения пульта или информация об энергии
        updateInfoItem();
    }

    public void updateInfoItem() {
        int energy = storage.getEnergy(terminalBlock);
        
        if (energy >= MAX_ENERGY) {
            ItemStack remoteReady = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = remoteReady.getItemMeta();
            meta.setDisplayName("§aОрбитальный Пульт готов!");
            meta.setLore(List.of(
                    "§7Энергия: §a" + energy + " / " + MAX_ENERGY,
                    "",
                    "§eНажмите сюда, чтобы получить пульт."
            ));
            remoteReady.setItemMeta(meta);
            inventory.setItem(15, remoteReady);
        } else {
            ItemStack info = new ItemStack(Material.REDSTONE_TORCH);
            ItemMeta meta = info.getItemMeta();
            meta.setDisplayName("§eСтатус зарядки");
            meta.setLore(List.of(
                    "§7Энергия: §e" + energy + " §7/ §a" + MAX_ENERGY,
                    "",
                    "§fПоложите уголь или лаву",
                    "§fв слот слева для зарядки."
            ));
            info.setItemMeta(meta);
            inventory.setItem(15, info);
        }
    }

    public Block getTerminalBlock() {
        return terminalBlock;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
