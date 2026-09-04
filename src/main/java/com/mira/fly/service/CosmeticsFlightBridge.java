package com.mira.fly.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public final class CosmeticsFlightBridge implements FlightEffectRenderer {
    private final JavaPlugin plugin;
    private Object api;
    private Method playFly;
    private long lastHookAttempt;

    public CosmeticsFlightBridge(JavaPlugin plugin) {
        this.plugin = plugin;
        hook();
    }

    @Override
    public boolean available() {
        if (api == null && System.currentTimeMillis() - lastHookAttempt >= 30_000L) hook();
        return api != null && playFly != null;
    }

    @Override
    public void play(Player player) {
        if (player == null || !available()) return;
        try {
            playFly.invoke(api, player);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("MiraCosmetics flight renderer became unavailable: "
                    + exception.getMessage());
            api = null;
            playFly = null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void hook() {
        lastHookAttempt = System.currentTimeMillis();
        if (!Bukkit.getPluginManager().isPluginEnabled("MiraCosmetics")) return;

        try {
            Class<?> apiType = Class.forName("gg.mira.cosmetics.MiraCosmeticsPlugin$CosmeticsApi");
            RegisteredServiceProvider<?> registration =
                    Bukkit.getServicesManager().getRegistration((Class) apiType);
            if (registration == null || registration.getProvider() == null) return;

            api = registration.getProvider();
            playFly = apiType.getMethod("playFly", Player.class);
            plugin.getLogger().info("MiraCosmetics flight-effect integration enabled.");
        } catch (ReflectiveOperationException exception) {
            api = null;
            playFly = null;
            plugin.getLogger().warning("MiraCosmetics is installed but its CosmeticsApi could not be hooked: "
                    + exception.getMessage());
        }
    }
}
