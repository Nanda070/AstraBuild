package com.astrabuild.gui;

import com.astrabuild.machine.NuclearReactorCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@SuppressWarnings("deprecation")
public class ReactorGui implements InventoryHolder {

    private final Inventory inventory;
    private final NuclearReactorCore reactor;

    public ReactorGui(NuclearReactorCore reactor) {
        this.reactor = reactor;
        this.inventory = Bukkit.createInventory(this, 27, "§cЯдерный Реактор");
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

        updateInfo();
    }

    public void updateInfo() {
        // Отрисовка статуса Нагрева
        int heat = reactor.getHeat();
        int maxHeat = NuclearReactorCore.MAX_HEAT;
        
        Material heatMat = Material.LIME_STAINED_GLASS;
        String heatColor = "§a";
        if (heat > maxHeat * 0.8) {
            heatMat = Material.RED_STAINED_GLASS;
            heatColor = "§c";
        } else if (heat > maxHeat * 0.5) {
            heatMat = Material.ORANGE_STAINED_GLASS;
            heatColor = "§6";
        }

        ItemStack heatItem = new ItemStack(heatMat);
        ItemMeta heatMeta = heatItem.getItemMeta();
        heatMeta.setDisplayName(heatColor + "Температура Ядра");
        heatMeta.setLore(List.of("§7" + heat + " / " + maxHeat));
        heatItem.setItemMeta(heatMeta);
        inventory.setItem(11, heatItem);

        // Отрисовка Энергии
        int energy = reactor.getGeneratedEnergy(); // Нужно добавить метод в NuclearReactorCore, но он сразу отправляет в сеть/блок
        ItemStack energyItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta energyMeta = energyItem.getItemMeta();
        energyMeta.setDisplayName("§eГенерация: " + energy + " EU/t");
        energyMeta.setLore(List.of("§7Передается в Energy Storage."));
        energyItem.setItemMeta(energyMeta);
        inventory.setItem(13, energyItem);

        // Отрисовка Охладителя
        ItemStack coolantItem = new ItemStack(Material.BLUE_ICE);
        ItemMeta coolantMeta = coolantItem.getItemMeta();
        coolantMeta.setDisplayName("§bВнутренний Охладитель");
        coolantMeta.setLore(List.of(
                "§7Запас: §b" + reactor.getCoolantMb() + " mB",
                "§8Поступает из Резервуаров Охладителя",
                "§8по сети труб."
        ));
        coolantItem.setItemMeta(coolantMeta);
        inventory.setItem(15, coolantItem);
    }

    public NuclearReactorCore getReactor() {
        return reactor;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}
