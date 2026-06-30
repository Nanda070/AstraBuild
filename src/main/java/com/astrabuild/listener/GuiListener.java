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

public class GuiListener implements Listener {

    private final com.astrabuild.AstraBuildPlugin plugin;
    private final MachineDataStorage storage;
    private final EventManager eventManager;
    private final RecipeRegistry recipeRegistry;

    public GuiListener(com.astrabuild.AstraBuildPlugin plugin, MachineDataStorage storage, EventManager eventManager, RecipeRegistry recipeRegistry) {
        this.plugin = plugin;
        this.storage = storage;
        this.eventManager = eventManager;
        this.recipeRegistry = recipeRegistry;
    }

    @SuppressWarnings("deprecation")
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
        } else if ("COOLANT_TANK".equals(type)) {
            event.setCancelled(true);
            com.astrabuild.machine.CoolantTankWorker worker = plugin.getIndustrialManager().getCoolantTanks().get(block.getLocation());
            if (worker != null) {
                com.astrabuild.gui.CoolantTankGui gui = new com.astrabuild.gui.CoolantTankGui(worker);
                gui.open(event.getPlayer());
            }
        } else if ("REACTOR_CORE".equals(type)) {
            if (handItem != null && handItem.hasItemMeta()) {
                String handType = recipeRegistry.getMachineItemType(handItem);
                if ("NANO_CHESTPLATE".equals(handType) || "NANO_SWORD".equals(handType)) {
                    event.setCancelled(true);
                    int blockEnergy = storage.getEnergy(block);
                    org.bukkit.persistence.PersistentDataContainer pdc = handItem.getItemMeta().getPersistentDataContainer();
                    org.bukkit.NamespacedKey energyKey = new org.bukkit.NamespacedKey(plugin, "astrabuild_energy");
                    Integer itemEnergy = pdc.get(energyKey, org.bukkit.persistence.PersistentDataType.INTEGER);
                    if (itemEnergy == null) itemEnergy = 0;
                    
                    int maxItemEnergy = "NANO_CHESTPLATE".equals(handType) ? 10000 : 5000;
                    int needed = maxItemEnergy - itemEnergy;
                    
                    if (needed > 0 && blockEnergy > 0) {
                        int toTransfer = Math.min(needed, blockEnergy);
                        storage.consumeEnergy(block, toTransfer);
                        itemEnergy += toTransfer;
                        
                        org.bukkit.inventory.meta.ItemMeta meta = handItem.getItemMeta();
                        meta.getPersistentDataContainer().set(energyKey, org.bukkit.persistence.PersistentDataType.INTEGER, itemEnergy);
                        java.util.List<String> lore = meta.getLore();
                        if (lore != null && lore.size() >= 4) {
                            lore.set(3, "§eЗаряд: §a" + itemEnergy + " §7/ §a" + maxItemEnergy + " EU");
                            meta.setLore(lore);
                        }
                        handItem.setItemMeta(meta);
                        
                        event.getPlayer().playSound(event.getPlayer().getLocation(), org.bukkit.Sound.BLOCK_BEACON_AMBIENT, 1.0f, 2.0f);
                        event.getPlayer().sendMessage("§a[+] Предмет заряжен на " + toTransfer + " EU от Реактора!");
                        return;
                    } else if (needed == 0) {
                        event.getPlayer().sendMessage("§cПредмет уже полностью заряжен!");
                        return;
                    }
                }
            }

            event.setCancelled(true);
            com.astrabuild.machine.NuclearReactorCore reactor = plugin.getIndustrialManager().getReactors().get(block.getLocation());
            if (reactor != null) {
                com.astrabuild.gui.ReactorGui gui = new com.astrabuild.gui.ReactorGui(reactor);
                gui.open(event.getPlayer());
            }
        } else if ("ENERGY_STORAGE".equals(type)) {
            event.setCancelled(true);
            com.astrabuild.gui.EnergyStorageGui gui = new com.astrabuild.gui.EnergyStorageGui(block, storage, plugin);
            event.getPlayer().openInventory(gui.getInventory());
        } else if ("CENTRIFUGE".equals(type)) {
            event.setCancelled(true);
            com.astrabuild.gui.CentrifugeGui gui = new com.astrabuild.gui.CentrifugeGui(block, storage, plugin);
            event.getPlayer().openInventory(gui.getInventory());
        } else if ("MASS_FABRICATOR".equals(type)) {
            event.setCancelled(true);
            com.astrabuild.gui.MassFabricatorGui gui = new com.astrabuild.gui.MassFabricatorGui(block, storage, plugin);
            event.getPlayer().openInventory(gui.getInventory());
        } else if ("CONNECTOR_INSERT".equals(type)) {
            event.setCancelled(true);
            com.astrabuild.gui.ConnectorInsertGui gui = new com.astrabuild.gui.ConnectorInsertGui(block, plugin);
            event.getPlayer().openInventory(gui.getInventory());
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
        } else if (event.getInventory().getHolder() instanceof com.astrabuild.gui.CoolantTankGui gui) {
            handleCoolantTankGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof com.astrabuild.gui.ReactorGui) {
            event.setCancelled(true); // Реактор только для чтения
        } else if (event.getInventory().getHolder() instanceof com.astrabuild.gui.CentrifugeGui gui) {
            handleCentrifugeGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof com.astrabuild.gui.MassFabricatorGui gui) {
            handleMassFabricatorGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof com.astrabuild.gui.EnergyStorageGui gui) {
            handleEnergyStorageGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof com.astrabuild.gui.ConnectorInsertGui gui) {
            handleConnectorInsertGuiClick(event, gui);
        } else if (event.getInventory().getHolder() instanceof com.astrabuild.gui.GuideMenuGui gui) {
            gui.handleClick(event, (org.bukkit.entity.Player) event.getWhoClicked());
        }
    }

    private void handleCoolantTankGuiClick(InventoryClickEvent event, com.astrabuild.gui.CoolantTankGui gui) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (slot >= 27 && clickedItem != null && !clickedItem.getType().isAir()) {
            int amount = 0;
            boolean returnBucket = false;

            switch (clickedItem.getType()) {
                case WATER_BUCKET -> {
                    amount = 1000;
                    returnBucket = true;
                }
                case ICE -> amount = 1000;
                case PACKED_ICE -> amount = 5000;
                case BLUE_ICE -> amount = 25000;
                default -> {}
            }

            if (amount > 0) {
                com.astrabuild.machine.CoolantTankWorker worker = gui.getWorker();
                if (worker.getCoolantMb() + amount <= com.astrabuild.machine.CoolantTankWorker.MAX_COOLANT) {
                    worker.addCoolant(amount);
                    clickedItem.setAmount(clickedItem.getAmount() - 1);
                    if (returnBucket) {
                        player.getInventory().addItem(new ItemStack(Material.BUCKET));
                    }
                    gui.updateInfoItem();
                } else {
                    player.sendMessage("§cРезервуар полон!");
                }
            }
        }
    }

    private void handleQuarryGuiClick(InventoryClickEvent event, QuarryGui gui) {
        event.setCancelled(true);

        org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
        org.bukkit.inventory.ItemStack clickedItem = event.getCurrentItem();
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
    private void handleCentrifugeGuiClick(org.bukkit.event.inventory.InventoryClickEvent event, com.astrabuild.gui.CentrifugeGui gui) {
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 27 && slot != 10 && slot != 15 && slot != 16) {
            event.setCancelled(true);
        }
        scheduleGuiSync(() -> gui.syncToBlock());
    }

    private void handleMassFabricatorGuiClick(org.bukkit.event.inventory.InventoryClickEvent event, com.astrabuild.gui.MassFabricatorGui gui) {
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 27 && slot != 11 && slot != 15) {
            event.setCancelled(true);
        }
        scheduleGuiSync(() -> gui.syncToBlock());
    }

    private void handleEnergyStorageGuiClick(org.bukkit.event.inventory.InventoryClickEvent event, com.astrabuild.gui.EnergyStorageGui gui) {
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 27 && slot != 11) {
            event.setCancelled(true);
        }
        scheduleGuiSync(() -> gui.syncToBlock());
    }

    private void handleConnectorInsertGuiClick(org.bukkit.event.inventory.InventoryClickEvent event, com.astrabuild.gui.ConnectorInsertGui gui) {
        if (event.isShiftClick()) {
            event.setCancelled(true);
            return;
        }
        int slot = event.getRawSlot();
        if (slot < 27 && !(slot >= 3 && slot <= 5 || slot >= 12 && slot <= 14 || slot >= 21 && slot <= 23)) {
            event.setCancelled(true);
        }
        scheduleGuiSync(() -> gui.syncToBlock());
    }

    private void scheduleGuiSync(Runnable syncTask) {
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, syncTask, 1L);
    }
}
