package com.mira.fly.service;

import org.bukkit.entity.Player;

public interface FlightEffectRenderer {
    boolean available();
    void play(Player player);
}
