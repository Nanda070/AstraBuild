package com.astrabuild.visual;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final org.bukkit.NamespacedKey holoKey;
    private final Map<Location, TextDisplay> activeHolograms = new ConcurrentHashMap<>();

    public HologramManager(Plugin plugin) {
        this.holoKey = new org.bukkit.NamespacedKey(plugin, "astrabuild_holo");
    }

    @SuppressWarnings("deprecation")
    public void updateHologram(Location loc, String text) {
        TextDisplay display = activeHolograms.get(loc);

        if (display == null || !display.isValid()) {
            if (loc.getWorld() == null || !loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) return;
            
            // Зачистка возможных дубликатов на этих координатах перед спавном
            clearAbandonedAt(loc);

            display = loc.getWorld().spawn(loc.clone().add(0.5, 1.5, 0.5), TextDisplay.class, ent -> {
                ent.setBillboard(Display.Billboard.CENTER);
                ent.setDefaultBackground(false);
                ent.setBackgroundColor(org.bukkit.Color.fromARGB(100, 0, 0, 0));
                ent.getPersistentDataContainer().set(holoKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            });
            activeHolograms.put(loc, display);
        }

        display.setText(text);
    }

    public void removeHologram(Location loc) {
        TextDisplay display = activeHolograms.remove(loc);
        if (display != null && display.isValid()) {
            display.remove();
        }
        if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
            sweepHologramsAt(loc);
        }
    }

    // Вызывается перед спавном новой голограммы — удаляет дубликаты, не трогая активную
    private void clearAbandonedAt(org.bukkit.Location loc) {
        if (loc.getWorld() == null) return;
        org.bukkit.Location center = loc.clone().add(0.5, 1.5, 0.5);
        for (org.bukkit.entity.Entity ent : loc.getWorld().getNearbyEntities(center, 1.0, 2.0, 1.0)) {
            if (ent instanceof org.bukkit.entity.TextDisplay textDisplay &&
                    textDisplay.getPersistentDataContainer().has(holoKey, org.bukkit.persistence.PersistentDataType.BYTE) &&
                    !activeHolograms.containsValue(textDisplay)) {
                textDisplay.remove();
            }
        }
    }

    // Вызывается при удалении машины — удаляет ВСЕ astrabuild-голограммы на этом месте
    private void sweepHologramsAt(org.bukkit.Location loc) {
        org.bukkit.Location center = loc.clone().add(0.5, 1.5, 0.5);
        for (org.bukkit.entity.Entity ent : loc.getWorld().getNearbyEntities(center, 1.0, 2.0, 1.0)) {
            if (ent instanceof org.bukkit.entity.TextDisplay textDisplay &&
                    textDisplay.getPersistentDataContainer().has(holoKey, org.bukkit.persistence.PersistentDataType.BYTE)) {
                textDisplay.remove();
            }
        }
    }

    // Удаляет осиротевшие голограммы в чанке (машина удалена, сущность пережила рестарт)
    public void cleanupOrphansInChunk(org.bukkit.Chunk chunk, com.astrabuild.storage.MachineDataStorage storage) {
        for (org.bukkit.entity.Entity ent : chunk.getEntities()) {
            if (!(ent instanceof org.bukkit.entity.TextDisplay td)) continue;
            if (!td.getPersistentDataContainer().has(holoKey, org.bukkit.persistence.PersistentDataType.BYTE)) continue;
            if (activeHolograms.containsValue(td)) continue;
            // Голограмма спавнится на block + (0.5, 1.5, 0.5); вычитаем 1 по Y чтобы попасть в блок машины
            org.bukkit.block.Block machineBlock = td.getLocation().subtract(0, 1, 0).getBlock();
            if (storage.getMachineType(machineBlock) == null) {
                td.remove();
            }
        }
    }

    public void cleanupAll() {
        for (TextDisplay display : activeHolograms.values()) {
            if (display.isValid()) {
                display.remove();
            }
        }
        activeHolograms.clear();
    }
}

