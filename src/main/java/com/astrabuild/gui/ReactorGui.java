package com.astrabuild.gui;

import com.astrabuild.machine.NuclearReactorCore;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
        this.inventory = Bukkit.createInventory(this, 27, LegacyComponentSerializer.legacySection().deserialize("§cЯдерный Реактор"));
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
        // ÐžÑ‚Ñ€Ð¸ÑÐ¾Ð²ÐºÐ° ÑÑ‚Ð°Ñ‚ÑƒÑÐ° ÐÐ°Ð³Ñ€ÐµÐ²Ð°
        int heat = reactor.getHeat();
        int maxHeat = NuclearReactorCore.MAX_HEAT;
        
        Material heatMat = Material.LIME_STAINED_GLASS;
        String heatColor = "Â§a";
        if (heat > maxHeat * 0.8) {
            heatMat = Material.RED_STAINED_GLASS;
            heatColor = "Â§c";
        } else if (heat > maxHeat * 0.5) {
            heatMat = Material.ORANGE_STAINED_GLASS;
            heatColor = "Â§6";
        }

        ItemStack heatItem = new ItemStack(heatMat);
        ItemMeta heatMeta = heatItem.getItemMeta();
        heatMeta.setDisplayName(heatColor + "Ð¢ÐµÐ¼Ð¿ÐµÑ€Ð°Ñ‚ÑƒÑ€Ð° Ð¯Ð´Ñ€Ð°");
        heatMeta.setLore(List.of("Â§7" + heat + " / " + maxHeat));
        heatItem.setItemMeta(heatMeta);
        inventory.setItem(11, heatItem);

        // ÐžÑ‚Ñ€Ð¸ÑÐ¾Ð²ÐºÐ° Ð­Ð½ÐµÑ€Ð³Ð¸Ð¸
        int energy = reactor.getGeneratedEnergy(); // ÐÑƒÐ¶Ð½Ð¾ Ð´Ð¾Ð±Ð°Ð²Ð¸Ñ‚ÑŒ Ð¼ÐµÑ‚Ð¾Ð´ Ð² NuclearReactorCore, Ð½Ð¾ Ð¾Ð½ ÑÑ€Ð°Ð·Ñƒ Ð¾Ñ‚Ð¿Ñ€Ð°Ð²Ð»ÑÐµÑ‚ Ð² ÑÐµÑ‚ÑŒ/Ð±Ð»Ð¾Ðº
        ItemStack energyItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta energyMeta = energyItem.getItemMeta();
        energyMeta.setDisplayName("Â§eÐ“ÐµÐ½ÐµÑ€Ð°Ñ†Ð¸Ñ: " + energy + " EU/t");
        energyMeta.setLore(List.of("Â§7ÐŸÐµÑ€ÐµÐ´Ð°ÐµÑ‚ÑÑ Ð² Energy Storage."));
        energyItem.setItemMeta(energyMeta);
        inventory.setItem(13, energyItem);

        // ÐžÑ‚Ñ€Ð¸ÑÐ¾Ð²ÐºÐ° ÐžÑ…Ð»Ð°Ð´Ð¸Ñ‚ÐµÐ»Ñ
        ItemStack coolantItem = new ItemStack(Material.BLUE_ICE);
        ItemMeta coolantMeta = coolantItem.getItemMeta();
        coolantMeta.setDisplayName("Â§bÐ’Ð½ÑƒÑ‚Ñ€ÐµÐ½Ð½Ð¸Ð¹ ÐžÑ…Ð»Ð°Ð´Ð¸Ñ‚ÐµÐ»ÑŒ");
        coolantMeta.setLore(List.of(
                "Â§7Ð—Ð°Ð¿Ð°Ñ: Â§b" + reactor.getCoolantMb() + " mB",
                "Â§8ÐŸÐ¾ÑÑ‚ÑƒÐ¿Ð°ÐµÑ‚ Ð¸Ð· Ð ÐµÐ·ÐµÑ€Ð²ÑƒÐ°Ñ€Ð¾Ð² ÐžÑ…Ð»Ð°Ð´Ð¸Ñ‚ÐµÐ»Ñ",
                "Â§8Ð¿Ð¾ ÑÐµÑ‚Ð¸ Ñ‚Ñ€ÑƒÐ±."
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
