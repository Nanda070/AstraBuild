package com.astrabuild.gui;

import com.astrabuild.machine.CoolantTankWorker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@SuppressWarnings("deprecation")
public class CoolantTankGui implements InventoryHolder {

    private final Inventory inventory;
    private final CoolantTankWorker worker;

    public CoolantTankGui(CoolantTankWorker worker) {
        this.worker = worker;
        this.inventory = Bukkit.createInventory(this, 27, "§bРезервуар Охладителя");
        setupLayout();
    }

    private void setupLayout() {
        ItemStack bg = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, bg);
        }

        // Слот 11 - Для ввода ведер с водой или льда
        inventory.setItem(11, new ItemStack(Material.AIR));

        // Слот 15 - Индикатор
        updateInfoItem();
    }

    public void updateInfoItem() {
        ItemStack info = new ItemStack(Material.WATER_BUCKET);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§bСтатус Охладителя");
        meta.setLore(List.of(
                "§7Охладитель: §b" + worker.getCoolantMb() + " §7/ §a" + CoolantTankWorker.MAX_COOLANT + " mB",
                "",
                "§fПоложите Ведро воды (1000mB)",
                "§fили Плотный лед (5000mB)",
                "§fв слот слева для заправки.",
                "§8Также может качать воду",
                "§8из бесконечного источника рядом."
        ));
        info.setItemMeta(meta);
        inventory.setItem(15, info);
    }

    public CoolantTankWorker getWorker() {
        return worker;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
