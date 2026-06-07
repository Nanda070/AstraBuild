package com.astrabuild.events;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EventManager {

    private final JavaPlugin plugin;
    private EventType currentEvent;
    private long nextEventTimestamp;
    private String webhookUrl;
    private String mentionRoleId;

    public EventManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        startTask();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        webhookUrl = config.getString("webhook-url", "");
        mentionRoleId = config.getString("mention-role-id", "");
        
        try {
            currentEvent = EventType.valueOf(config.getString("current-event", "NORMAL").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            currentEvent = EventType.NORMAL;
        }
        
        nextEventTimestamp = config.getLong("next-event-timestamp", 0);
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime >= nextEventTimestamp) {
                    rollNewEvent();
                }
            }
        }.runTaskTimer(plugin, 20L * 60L, 20L * 60L); // Раз в минуту
    }

    private void rollNewEvent() {
        List<EventType> availableEvents = Arrays.stream(EventType.values())
                .filter(e -> e != currentEvent)
                .collect(Collectors.toList());

        EventType newEvent = availableEvents.get(new Random().nextInt(availableEvents.size()));
        this.currentEvent = newEvent;
        // +24 hours
        this.nextEventTimestamp = System.currentTimeMillis() + (24L * 60 * 60 * 1000);

        FileConfiguration config = plugin.getConfig();
        config.set("current-event", newEvent.name());
        config.set("next-event-timestamp", nextEventTimestamp);
        plugin.saveConfig();

        sendDiscordNotification(newEvent);
    }

    public EventType getCurrentEvent() {
        return currentEvent;
    }

    private void sendDiscordNotification(EventType newEvent) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = java.net.URI.create(webhookUrl).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setDoOutput(true);

                String jsonPayload = String.format(
                        "{\"content\": \"<@&%s> Мудрецы объявили неделю %s! Эффект: %s\"}",
                        mentionRoleId, newEvent.getTitle(), newEvent.getDescription()
                );

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                connection.getResponseCode();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send discord notification: " + e.getMessage());
            }
        });
    }
}
