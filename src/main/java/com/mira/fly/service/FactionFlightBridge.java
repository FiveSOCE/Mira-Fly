package com.mira.fly.service;

import com.mira.factions.api.FactionFlightController;
import com.mira.factions.api.MiraFactionsApi;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class FactionFlightBridge implements FactionAccess, FactionFlightController {
    private final MiraFactionsApi factions;
    private final FlyManager manager;

    public FactionFlightBridge(MiraFactionsApi factions, FlyManager manager) {
        this.factions = factions;
        this.manager = manager;
    }

    @Override
    public boolean available() {
        return factions != null;
    }

    @Override
    public boolean entitled(Player player) {
        return factions != null && factions.hasFactionFlightEntitlement(player);
    }

    @Override
    public String territory(Player player) {
        return factions == null ? "UNMANAGED" : factions.flightTerritory(player, player.getLocation()).name();
    }

    @Override
    public ToggleResult toggle(Player player) {
        return manager.toggleFaction(player);
    }

    @Override
    public boolean active(UUID playerId) {
        return manager.isFactionActive(playerId);
    }

    @Override
    public void refresh(Player player) {
        manager.refresh(player);
    }
}
