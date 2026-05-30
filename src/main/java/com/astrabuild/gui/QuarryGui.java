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

public class QuarryGui implements InventoryHolder {

    private final Inventory inventory;
    private final Block quarryBlock;
    private final MachineDataStorage storage;

    public QuarryGui(Block quarryBlock, MachineDataStorage storage) {
        this.quarryBlock = quarryBlock;
        this.storage = storage;
        // Размер кратен 9. 27 = 3 ряда.
        this.inventory = Bukkit.createInventory(this, 27, "§8Контроллер карьера");
        setupLayout();
    }

    private void setupLayout() {
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, bg);
        }

        // Слот 11 - Для ввода топлива (оставляем пустым)
        inventory.setItem(11, new ItemStack(Material.AIR));

        // Слот 15 - Информация об энергии
        updateInfoItem();
    }

    public void updateInfoItem() {
        ItemStack info = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§eСтатус карьера");
        
        int energy = storage.getEnergy(quarryBlock);
        meta.setLore(List.of(
                "§7Запас энергии: §a" + energy,
                "§7Потребление: §c1 ед/блок",
                "",
                "§fПоложите уголь или ведро лавы",
                "§fв пустой слот слева для зарядки."
        ));
        info.setItemMeta(meta);
        inventory.setItem(15, info);
    }

    public Block getQuarryBlock() {
        return quarryBlock;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}