package com.mira.fly.service;

import org.bukkit.entity.Player;

public interface FactionAccess {
    boolean available();
    boolean entitled(Player player);
    String territory(Player player);
}
