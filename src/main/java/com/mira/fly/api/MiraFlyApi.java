package com.mira.fly.api;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface MiraFlyApi {
    long remaining(UUID player);
    long addTime(UUID player, long seconds);
    boolean active(UUID player);
    boolean factionActive(UUID player);
    boolean toggle(Player player);
}
