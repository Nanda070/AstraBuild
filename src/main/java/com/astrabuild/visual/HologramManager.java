package com.astrabuild.visual;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HologramManager {

    private final Plugin plugin;
    private final Map<Location, TextDisplay> activeHolograms = new ConcurrentHashMap<>();

    public HologramManager(Plugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public void updateHologram(Location loc, String text) {
        TextDisplay display = activeHolograms.get(loc);

        if (display == null || !display.isValid()) {
            if (loc.getWorld() == null || !loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) return;

            display = loc.getWorld().spawn(loc.clone().add(0.5, 1.5, 0.5), TextDisplay.class, ent -> {
                ent.setBillboard(Display.Billboard.CENTER);
                ent.setDefaultBackground(false);
                ent.setBackgroundColor(org.bukkit.Color.fromARGB(100, 0, 0, 0));
                ent.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "astrabuild_holo"), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
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
