package com.mira.fly.service;

import com.mira.fly.MiraFlyPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FlyManager {
    private final MiraFlyPlugin plugin;
    private final FlyTimeService time;
    private final Set<UUID> active = new HashSet<>();
    private BukkitTask task;
    private int saveCounter;

    public FlyManager(MiraFlyPlugin plugin, FlyTimeService time) {
        this.plugin = plugin;
        this.time = time;
    }

    public void start() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (active.remove(uuid)) {
            disableFlight(player);
            send(player, player.hasPermission("mirafly.permanent") ? "permanent-disabled" : "disabled");
            return false;
        }

        if (!player.hasPermission("mirafly.permanent")) {
            if (!player.hasPermission("mirafly.use")) {
                send(player, "no-permission");
                return false;
            }
            if (time.get(uuid) <= 0L) {
                send(player, "no-access");
                return false;
            }
        }

        active.add(uuid);
        player.setAllowFlight(true);
        send(player, player.hasPermission("mirafly.permanent") ? "permanent-enabled" : "enabled");
        return true;
    }

    public boolean isActive(UUID uuid) {
        return active.contains(uuid);
    }

    public void deactivate(Player player) {
        if (active.remove(player.getUniqueId())) disableFlight(player);
    }

    public void shutdown() {
        if (task != null) task.cancel();
        for (Player player : plugin.getServer().getOnlinePlayers()) deactivate(player);
        time.save();
    }

    private void tick() {
        for (UUID uuid : new HashSet<>(active)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                active.remove(uuid);
                continue;
            }
            if (player.hasPermission("mirafly.permanent")) continue;
            if (!player.hasPermission("mirafly.use")) {
                active.remove(uuid);
                disableFlight(player);
                send(player, "no-permission");
                continue;
            }

            long remaining = time.consumeOne(uuid);
            if (remaining == 60L) send(player, "warning-60");
            else if (remaining == 30L) send(player, "warning-30");
            else if (remaining == 10L) send(player, "warning-10");
            else if (remaining <= 0L) {
                active.remove(uuid);
                disableFlight(player);
                send(player, "expired");
            }
        }

        saveCounter++;
        if (saveCounter >= 30) {
            saveCounter = 0;
            time.save();
        }
    }

    private void disableFlight(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying()) player.setFlying(false);
        player.setAllowFlight(false);
    }

    private void send(Player player, String key) {
        String message = plugin.getConfig().getString("messages." + key, "")
                .replace("%time%", FlyTimeService.format(time.get(player.getUniqueId())));
        if (!message.isBlank()) player.sendMessage(FlyVoucherService.component(message));
    }
}
