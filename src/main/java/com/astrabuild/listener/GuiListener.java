package com.astrabuild.listener;

import com.astrabuild.gui.QuarryGui;
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

    public GuiListener(Plugin plugin, MachineDataStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Block block = event.getClickedBlock();
        if (block == null) return;

        String type = storage.getMachineType(block);
        if ("QUARRY".equals(type)) {
            event.setCancelled(true);
            QuarryGui gui = new QuarryGui(block, storage);
            gui.open(event.getPlayer());

            // Читаем координаты из памяти блока и запускаем лазерную сетку
            Location[] bounds = storage.getQuarryBoundsParsed(block);
            if (bounds != null) {
                DisplayUtil.highlightBounds(plugin, block.getLocation(), bounds[0], bounds[1]);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof QuarryGui gui)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        int slot = event.getRawSlot();

        if (slot >= 27 && clickedItem != null && !clickedItem.getType().isAir()) {
            int energyValue = getFuelValue(clickedItem.getType());
            
            if (energyValue > 0) {
                storage.addEnergy(gui.getQuarryBlock(), energyValue);
                clickedItem.setAmount(clickedItem.getAmount() - 1);
                
                if (clickedItem.getType() == Material.LAVA_BUCKET) {
                    player.getInventory().addItem(new ItemStack(Material.BUCKET));
                }
                gui.updateInfoItem();
            }
        }
    }

    private int getFuelValue(Material material) {
        return switch (material) {
            case COAL, CHARCOAL -> 80;
            case COAL_BLOCK -> 800;
            case LAVA_BUCKET -> 1000;
            case BLAZE_ROD -> 120;
            default -> 0;
        };
    }
}