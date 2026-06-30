package com.astrabuild.item;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

@SuppressWarnings("deprecation")
public class UraniumItems {

    private final NamespacedKey itemTypeKey;

    public UraniumItems(Plugin plugin) {
        this.itemTypeKey = new NamespacedKey(plugin, "machine_item_type");
    }

    public ItemStack getItem(String type) {
        ItemStack item = switch (type) {
            case "URANIUM_RAW" -> createRawUranium();
            case "URANIUM_ISOTOPE" -> createUraniumIsotope();
            case "FUEL_ROD" -> createFuelRod();
            case "DEPLETED_FUEL_ROD" -> createDepletedFuelRod();
            case "HAZMAT_HELMET" -> createHazmatArmor(Material.LEATHER_HELMET, "Шлем Химзащиты", "HAZMAT_HELMET");
            case "HAZMAT_CHESTPLATE" -> createHazmatArmor(Material.LEATHER_CHESTPLATE, "Куртка Химзащиты", "HAZMAT_CHESTPLATE");
            case "HAZMAT_LEGGINGS" -> createHazmatArmor(Material.LEATHER_LEGGINGS, "Штаны Химзащиты", "HAZMAT_LEGGINGS");
            case "HAZMAT_BOOTS" -> createHazmatArmor(Material.LEATHER_BOOTS, "Ботинки Химзащиты", "HAZMAT_BOOTS");
            case "TOXIC_WASTE" -> createToxicWaste();
            default -> null;
        };
        
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            int cmdId = getCustomModelDataId(type);
            if (cmdId != -1) {
                meta.setCustomModelData(cmdId);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    public static int getCustomModelDataId(String type) {
        return switch(type) {
            case "QUARRY" -> 458001;
            case "PIPE" -> 458002;
            case "CONNECTOR_INSERT" -> 458003;
            case "UPGRADE_FORTUNE" -> 458004;
            case "UPGRADE_SILK_TOUCH" -> 458005;
            case "UPGRADE_OVERCLOCKER" -> 458006;
            case "SATELLITE_TERMINAL" -> 458007;
            case "ORBITAL_REMOTE" -> 458008;
            case "COOLANT_TANK" -> 458009;
            case "REACTOR_CORE" -> 458010;
            case "CENTRIFUGE" -> 458011;
            case "MASS_FABRICATOR" -> 458012;
            case "ENERGY_STORAGE" -> 458013;
            case "NANO_CHESTPLATE" -> 458014;
            case "NANO_SWORD" -> 458015;
            case "URANIUM_RAW" -> 458016;
            case "URANIUM_ISOTOPE" -> 458017;
            case "FUEL_ROD" -> 458018;
            case "DEPLETED_FUEL_ROD" -> 458019;
            case "TOXIC_WASTE" -> 458020;
            case "HAZMAT_HELMET" -> 458021;
            case "HAZMAT_CHESTPLATE" -> 458022;
            case "HAZMAT_LEGGINGS" -> 458023;
            case "HAZMAT_BOOTS" -> 458024;
            default -> -1;
        };
    }

    private ItemStack createRawUranium() {
        ItemStack item = new ItemStack(Material.RAW_IRON);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§2Необработанный Уран");
        meta.setLore(List.of("§7Радиоактивная руда.", "§7Нужно обогащение в Центрифуге."));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, "URANIUM_RAW");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUraniumIsotope() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aИзотоп Урана-235");
        meta.setLore(List.of("§7Высокообогащенный уран.", "§7Используется для создания топливных стержней."));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, "URANIUM_ISOTOPE");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFuelRod() {
        ItemStack item = new ItemStack(Material.END_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aТопливный Стержень");
        meta.setLore(List.of("§7Топливо для ядерного реактора."));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, "FUEL_ROD");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createDepletedFuelRod() {
        ItemStack item = new ItemStack(Material.END_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§8Отработанный Стержень");
        meta.setLore(List.of("§cУГРОЗА: Высокая радиация!", "§7Подлежит утилизации."));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, "DEPLETED_FUEL_ROD");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createToxicWaste() {
        ItemStack item = new ItemStack(Material.SLIME_BALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§2Токсичные отходы");
        meta.setLore(List.of("§cУГРОЗА: Побочный продукт обогащения.", "§7Наносят сильнейший урон."));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, "TOXIC_WASTE");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createHazmatArmor(Material material, String name, String typeId) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setDisplayName("§e" + name);
        meta.setColor(Color.fromRGB(220, 220, 0));
        meta.setLore(List.of("§7Защищает от радиации.", "§cПолный сет блокирует весь урон от радиации."));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, typeId);
        item.setItemMeta(meta);
        return item;
    }
}
