package com.mira.fly.service;

import com.mira.factions.api.FactionFlightController;
import com.mira.fly.MiraFlyPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class FlyManager {
    private final MiraFlyPlugin plugin;
    private final FlyTimeService time;
    private final Set<UUID> personalActive = new HashSet<>();
    private final Set<UUID> factionActive = new HashSet<>();
    private final Set<UUID> regionBlocked = new HashSet<>();
    private FactionAccess factions;
    private BukkitTask task;
    private int saveCounter;

    public FlyManager(MiraFlyPlugin plugin, FlyTimeService time) {
        this.plugin = plugin;
        this.time = time;
    }

    public void setFactionAccess(FactionAccess factions) {
        this.factions = factions;
    }

    public void start() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public boolean toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (personalActive.remove(uuid)) {
            refresh(player);
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

        if (!regionAllowed(player, false)) {
            send(player, "region-blocked");
            return false;
        }

        personalActive.add(uuid);
        refresh(player);
        send(player, player.hasPermission("mirafly.permanent") ? "permanent-enabled" : "enabled");
        return true;
    }

    public FactionFlightController.ToggleResult toggleFaction(Player player) {
        UUID uuid = player.getUniqueId();
        if (factionActive.remove(uuid)) {
            refresh(player);
            return FactionFlightController.ToggleResult.disabled("Faction flight disabled.");
        }
        if (factions == null || !factions.available()) {
            return FactionFlightController.ToggleResult.failed("Faction flight requires MiraFactions integration.");
        }
        if (!factions.entitled(player)) {
            return FactionFlightController.ToggleResult.failed("Your faction flight entitlement is no longer valid.");
        }
        if (!regionAllowed(player, true)) {
            return FactionFlightController.ToggleResult.failed("Faction flight is not allowed in this territory.");
        }

        factionActive.add(uuid);
        refresh(player);
        return FactionFlightController.ToggleResult.enabled("Faction flight enabled.");
    }

    public boolean isActive(UUID uuid) {
        return personalActive.contains(uuid) || factionActive.contains(uuid);
    }

    public boolean isFactionActive(UUID uuid) {
        return factionActive.contains(uuid);
    }

    public void deactivate(Player player) {
        UUID uuid = player.getUniqueId();
        personalActive.remove(uuid);
        factionActive.remove(uuid);
        regionBlocked.remove(uuid);
        disableFlight(player);
    }

    public void refresh(Player player) {
        UUID uuid = player.getUniqueId();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        boolean personalRequested = personalActive.contains(uuid);
        boolean factionRequested = factionActive.contains(uuid);

        boolean permanent = player.hasPermission("mirafly.permanent");
        boolean personalAccess = personalRequested && (permanent || (player.hasPermission("mirafly.use") && time.get(uuid) > 0L));
        if (personalRequested && !personalAccess) personalActive.remove(uuid);

        boolean factionEntitled = factionRequested && factions != null && factions.available() && factions.entitled(player);
        if (factionRequested && !factionEntitled) factionActive.remove(uuid);

        boolean personalRegion = personalAccess && regionAllowed(player, false);
        boolean factionRegion = factionEntitled && regionAllowed(player, true);
        boolean allowed = personalRegion || factionRegion;

        if (allowed) {
            boolean wasBlocked = regionBlocked.remove(uuid);
            player.setAllowFlight(true);
            if (wasBlocked) send(player, "region-restored");
            return;
        }

        if ((personalAccess || factionEntitled) && regionBlocked.add(uuid)) send(player, "region-blocked");
        if (factionEntitled && !factionRegion) factionActive.remove(uuid);
        disableFlight(player);
    }

    public void shutdown() {
        if (task != null) task.cancel();
        for (Player player : plugin.getServer().getOnlinePlayers()) deactivate(player);
        time.save();
    }

    private void tick() {
        Set<UUID> tracked = new HashSet<>(personalActive);
        tracked.addAll(factionActive);

        for (UUID uuid : tracked) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                personalActive.remove(uuid);
                factionActive.remove(uuid);
                regionBlocked.remove(uuid);
                continue;
            }

            refresh(player);

            if (!personalActive.contains(uuid)) continue;
            if (player.hasPermission("mirafly.permanent")) continue;
            if (factionActive.contains(uuid) && factions != null && factions.entitled(player) && regionAllowed(player, true)) continue;
            if (!player.isFlying() || !regionAllowed(player, false)) continue;

            long remaining = time.consumeOne(uuid);
            if (remaining == 60L) send(player, "warning-60");
            else if (remaining == 30L) send(player, "warning-30");
            else if (remaining == 10L) send(player, "warning-10");
            else if (remaining <= 0L) {
                personalActive.remove(uuid);
                refresh(player);
                send(player, "expired");
            }
        }

        saveCounter++;
        if (saveCounter >= 30) {
            saveCounter = 0;
            time.save();
        }
    }

    private boolean regionAllowed(Player player, boolean factionMode) {
        for (String world : plugin.getConfig().getStringList("regions.blocked-worlds")) {
            if (world.equalsIgnoreCase(player.getWorld().getName())) return false;
        }

        if (factions == null || !factions.available()) return !factionMode;
        String territory = factions.territory(player).toUpperCase(Locale.ROOT);
        String path = factionMode ? "regions.faction-allowed-territories" : "regions.personal-allowed-territories";
        return plugin.getConfig().getStringList(path).stream().anyMatch(value -> value.equalsIgnoreCase(territory));
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
