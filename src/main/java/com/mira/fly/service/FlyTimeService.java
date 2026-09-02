package com.mira.fly.service;

import com.mira.fly.MiraFlyPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FlyTimeService {
    private final MiraFlyPlugin plugin;
    private final File file;
    private final Map<UUID, Long> seconds = new HashMap<>();

    public FlyTimeService(MiraFlyPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        reload();
    }

    public void reload() {
        seconds.clear();
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String key : yaml.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long value = Math.max(0L, yaml.getLong(key + ".seconds", 0L));
                if (value > 0L) seconds.put(uuid, value);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Skipping malformed UUID in players.yml: " + key);
            }
        }
    }

    public long get(UUID uuid) {
        return seconds.getOrDefault(uuid, 0L);
    }

    public long add(UUID uuid, long amount) {
        long updated = Math.max(0L, get(uuid) + Math.max(0L, amount));
        if (updated == 0L) seconds.remove(uuid); else seconds.put(uuid, updated);
        save();
        return updated;
    }

    public long consumeOne(UUID uuid) {
        long current = get(uuid);
        if (current <= 0L) return 0L;
        long updated = current - 1L;
        if (updated <= 0L) seconds.remove(uuid); else seconds.put(uuid, updated);
        return updated;
    }

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Long> entry : seconds.entrySet()) {
            yaml.set(entry.getKey() + ".seconds", entry.getValue());
        }
        try {
            yaml.save(file);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save MiraFly players.yml", ex);
        }
    }

    public static String format(long totalSeconds) {
        long seconds = Math.max(0L, totalSeconds);
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remaining = seconds % 60L;
        if (hours > 0L) return String.format("%dh %02dm %02ds", hours, minutes, remaining);
        return String.format("%dm %02ds", minutes, remaining);
    }
}
