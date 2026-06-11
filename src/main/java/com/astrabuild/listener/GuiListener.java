package com.astrabuild.listener;

import com.astrabuild.events.EventManager;
import com.astrabuild.events.EventType;
import com.astrabuild.gui.AdminGui;
import com.astrabuild.gui.CraftsGui;
import com.astrabuild.gui.QuarryGui;
import com.astrabuild.gui.RecipeViewGui;
import com.astrabuild.gui.SatelliteTerminalGui;
import com.astrabuild.item.RecipeRegistry;
import com.astrabuild.storage.MachineDataStorage;
import com.astrabuild.visual.DisplayUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class GuiListener implements Listener {

    private final Plugin plugin;
    private final MachineDataStorage storage;
    private final EventManager eventManager;
    private final RecipeRegistry recipeRegistry;

    public GuiListener(Plugin plugin, MachineDataStorage storage, EventManager eventManager, RecipeRegistry recipeRegistry) {
        this.plugin = plugin;
        this.storage = storage;
        this.eventManager = eventManager;
        this.recipeRegistry = recipeRegistry;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Не открываем GUI механизма, если в руке Пульт (его обрабатывает OrbitalStrikeListener)
        ItemStack handItem = event.getItem();
        if (handItem != null && recipeRegistry.getMachineItemType(handItem) != null
                && "ORBITAL_REMOTE".equals(recipeRegistry.getMachineItemType(handItem))) {
            return;
        }

        String type = storage.getMachineType(block);
        if ("QUARRY".equals(type)) {
            event.setCancelled(true);
            QuarryGui gui = new QuarryGui(block, storage, recipeRegistry);
            gui.open(event.getPlayer());

            // Читаем координаты из памяти блока и запускаем лазерную сетку
            Location[] bounds = storage.getQuarryBoundsParsed(block);
            if (bounds != null) {
                DisplayUtil.highlightBounds(plugin, block.getLocation(), bounds[0], bounds[1]);
            }
        } else if ("SATELLITE_TERMINAL".equals(type)) {
            event.setCancelled(true);
            SatelliteTerminalGui gui = new SatelliteTerminalGui(block, storage);
            gui.open(event.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof QuarryGui gui) {
            handleQuarryGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof SatelliteTerminalGui gui) {
            handleSatelliteTerminalGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof CraftsGui gui) {
            handleCraftsGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof AdminGui gui) {
            handleAdminGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof RecipeViewGui gui) {
            handleRecipeViewGuiClick(event, gui);
        }
    }

    private void handleQuarryGuiClick(InventoryClickEvent event, QuarryGui gui) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (slot >= 27 && clickedItem != null && !clickedItem.getType().isAir()) {
            String itemType = recipeRegistry.getMachineItemType(clickedItem);
            if (itemType != null && itemType.startsWith("UPGRADE_")) {
                if (storage.hasUpgrade(gui.getQuarryBlock(), itemType)) {
                    player.sendMessage("§cЭтот апгрейд уже установлен!");
                    return;
                }
                if (itemType.equals("UPGRADE_FORTUNE") && storage.hasUpgrade(gui.getQuarryBlock(), "UPGRADE_SILK_TOUCH")) {
                    player.sendMessage("§cНельзя совместить Удачу и Шелковое касание!");
                    return;
                }
                if (itemType.equals("UPGRADE_SILK_TOUCH") && storage.hasUpgrade(gui.getQuarryBlock(), "UPGRADE_FORTUNE")) {
                    player.sendMessage("§cНельзя совместить Удачу и Шелковое касание!");
                    return;
                }
                
                storage.setUpgrade(gui.getQuarryBlock(), itemType, true);
                clickedItem.setAmount(clickedItem.getAmount() - 1);
                gui.updateInfoItem();
                return;
            }

            int energyValue = getFuelValue(clickedItem.getType());
            
            if (energyValue > 0) {
                storage.addEnergy(gui.getQuarryBlock(), energyValue);
                clickedItem.setAmount(clickedItem.getAmount() - 1);
                
                if (clickedItem.getType() == Material.LAVA_BUCKET) {
                    player.getInventory().addItem(new ItemStack(Material.BUCKET));
                }
                gui.updateInfoItem();
            }
        } else if (slot == 12 || slot == 13 || slot == 14) {
            String upgradeType = switch(slot) {
                case 12 -> "UPGRADE_FORTUNE";
                case 13 -> "UPGRADE_SILK_TOUCH";
                case 14 -> "UPGRADE_OVERCLOCKER";
                default -> null;
            };
            if (upgradeType != null && storage.hasUpgrade(gui.getQuarryBlock(), upgradeType)) {
                storage.setUpgrade(gui.getQuarryBlock(), upgradeType, false);
                player.getInventory().addItem(recipeRegistry.getMachineItem(upgradeType));
                gui.updateInfoItem();
            }
        }
    }

    private void handleSatelliteTerminalGuiClick(InventoryClickEvent event, SatelliteTerminalGui gui) {
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (slot == 15) {
            if (storage.getEnergy(gui.getTerminalBlock()) >= SatelliteTerminalGui.MAX_ENERGY) {
                storage.consumeEnergy(gui.getTerminalBlock(), SatelliteTerminalGui.MAX_ENERGY);
                player.getInventory().addItem(recipeRegistry.getMachineItem("ORBITAL_REMOTE"));
                gui.updateInfoItem();
            }
        } else if (slot >= 27 && clickedItem != null && !clickedItem.getType().isAir()) {
            int energyValue = getFuelValue(clickedItem.getType());
            if (energyValue > 0) {
                int currentEnergy = storage.getEnergy(gui.getTerminalBlock());
                if (currentEnergy < SatelliteTerminalGui.MAX_ENERGY) {
                    storage.addEnergy(gui.getTerminalBlock(), energyValue);
                    clickedItem.setAmount(clickedItem.getAmount() - 1);
                    if (clickedItem.getType() == Material.LAVA_BUCKET) {
                        player.getInventory().addItem(new ItemStack(Material.BUCKET));
                    }
                    gui.updateInfoItem();
                }
            }
        }
    }

    private void handleCraftsGuiClick(InventoryClickEvent event, CraftsGui gui) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && !clicked.getType().isAir()) {
            new RecipeViewGui(clicked).open((Player) event.getWhoClicked());
        }
    }

    private void handleAdminGuiClick(InventoryClickEvent event, AdminGui gui) {
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && !clicked.getType().isAir()) {
            event.getWhoClicked().getInventory().addItem(clicked.clone());
        }
    }

    private void handleRecipeViewGuiClick(InventoryClickEvent event, RecipeViewGui gui) {
        event.setCancelled(true);
        if (event.getRawSlot() == 18) {
            new CraftsGui(recipeRegistry).open((Player) event.getWhoClicked());
        }
    }

    private int getFuelValue(Material material) {
        int baseValue = switch (material) {
            case COAL, CHARCOAL -> 80;
            case COAL_BLOCK -> 800;
            case LAVA_BUCKET -> 1000;
            case BLAZE_ROD -> 120;
            default -> 0;
        };

        if (baseValue > 0 && eventManager != null && eventManager.getCurrentEvent() == EventType.FUEL_SURPLUS) {
            return (int) (baseValue * 1.3);
        }

        return baseValue;
    }
}