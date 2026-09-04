package com.mira.fly;

import com.mira.core.api.MiraCore;
import com.mira.core.api.MiraCoreProvider;
import com.mira.core.api.ModuleHealth;
import com.mira.factions.api.FactionFlightController;
import com.mira.factions.api.MiraFactionsApi;
import com.mira.fly.api.MiraFlyApi;
import com.mira.fly.command.FlyCommand;
import com.mira.fly.listener.FlyVoucherListener;
import com.mira.fly.listener.VoucherProtectionListener;
import com.mira.fly.service.CosmeticsFlightBridge;
import com.mira.fly.service.FactionFlightBridge;
import com.mira.fly.service.FlyManager;
import com.mira.fly.service.FlyTimeService;
import com.mira.fly.service.FlyVoucherService;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public final class MiraFlyPlugin extends JavaPlugin implements Listener {
    private MiraCore core;
    private FlyTimeService time;
    private FlyVoucherService vouchers;
    private FlyManager manager;
    private FactionFlightBridge factionBridge;
    private CosmeticsFlightBridge cosmeticsBridge;
    private MiraFlyApi api;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        core = MiraCoreProvider.require();

        time = new FlyTimeService(this);
        vouchers = new FlyVoucherService(this);
        manager = new FlyManager(this, time);

        cosmeticsBridge = new CosmeticsFlightBridge(this);
        manager.setEffectRenderer(cosmeticsBridge);

        if (getServer().getPluginManager().isPluginEnabled("MiraFactions")) {
            MiraFactionsApi factions = getServer().getServicesManager().load(MiraFactionsApi.class);
            if (factions != null) {
                factionBridge = new FactionFlightBridge(factions, manager);
                manager.setFactionAccess(factionBridge);
                getServer().getServicesManager().register(
                        FactionFlightController.class, factionBridge, this, ServicePriority.Normal);
                getLogger().info("MiraFactions flight integration enabled. MiraFly owns runtime flight state.");
            } else {
                getLogger().warning("MiraFactions is enabled but its API service was not available; faction flight integration is disabled.");
            }
        }

        api = new MiraFlyApiImpl();
        getServer().getServicesManager().register(MiraFlyApi.class, api, this, ServicePriority.Normal);
        core.services().register(MiraFlyApi.class, api);
        core.modules().register(this, "MiraFly");
        core.modules().setHealth(this, ModuleHealth.HEALTHY,
                cosmeticsBridge.available()
                        ? "Timed/permanent/faction flight with MiraCosmetics flight rendering ready"
                        : "Timed/permanent/faction flight ready; MiraCosmetics flight rendering optional");

        FlyCommand commands = new FlyCommand(this, manager, time, vouchers);
        registerCommand("fly", commands);
        registerCommand("flytime", commands);
        registerCommand("flyvoucher", commands);

        getServer().getPluginManager().registerEvents(
                new FlyVoucherListener(this, vouchers, time), this);
        getServer().getPluginManager().registerEvents(new VoucherProtectionListener(vouchers), this);
        getServer().getPluginManager().registerEvents(this, this);

        manager.start();
        getServer().getOnlinePlayers().forEach(manager::refresh);
        getLogger().info("MiraFly v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.shutdown();
        else if (time != null) time.save();

        getServer().getServicesManager().unregisterAll(this);
        if (core != null) {
            if (api != null) core.services().unregister(MiraFlyApi.class, api);
            core.modules().unregister(this);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (manager != null) {
            getServer().getScheduler().runTask(this, () -> manager.refresh(event.getPlayer()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (manager != null) manager.deactivate(event.getPlayer());
        if (time != null) time.save();
    }

    public void audit(String action, CommandSender actor, String targetId,
                      String summary, Map<String, String> metadata) {
        if (core == null) return;
        core.audit().record("MiraFly", action,
                actor instanceof Player player ? player.getUniqueId() : null,
                actor == null ? "system" : actor.getName(),
                targetId, summary, metadata == null ? Map.of() : Map.copyOf(metadata));
    }

    private void registerCommand(String name, FlyCommand executor) {
        PluginCommand command = getCommand(name);
        if (command == null) throw new IllegalStateException(name + " command missing from plugin.yml");
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private final class MiraFlyApiImpl implements MiraFlyApi {
        @Override public long remaining(UUID player) { return time.get(player); }

        @Override
        public long addTime(UUID player, long seconds) {
            if (seconds <= 0L) return time.get(player);
            long updated = time.add(player, seconds);
            Player online = getServer().getPlayer(player);
            if (online != null) manager.refresh(online);
            return updated;
        }

        @Override public boolean active(UUID player) { return manager.isActive(player); }
        @Override public boolean factionActive(UUID player) { return manager.isFactionActive(player); }
        @Override public boolean toggle(Player player) { return manager.toggle(player); }
    }
}
