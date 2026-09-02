package com.mira.fly;

import com.mira.fly.command.FlyCommand;
import com.mira.fly.listener.FlyVoucherListener;
import com.mira.fly.listener.VoucherProtectionListener;
import com.mira.fly.service.FlyManager;
import com.mira.fly.service.FlyTimeService;
import com.mira.fly.service.FlyVoucherService;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class MiraFlyPlugin extends JavaPlugin implements Listener {
    private FlyTimeService time;
    private FlyVoucherService vouchers;
    private FlyManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        time = new FlyTimeService(this);
        vouchers = new FlyVoucherService(this);
        manager = new FlyManager(this, time);

        FlyCommand commands = new FlyCommand(this, manager, time, vouchers);
        registerCommand("fly", commands);
        registerCommand("flytime", commands);
        registerCommand("flyvoucher", commands);

        getServer().getPluginManager().registerEvents(new FlyVoucherListener(this, vouchers, time), this);
        getServer().getPluginManager().registerEvents(new VoucherProtectionListener(vouchers), this);
        getServer().getPluginManager().registerEvents(this, this);

        manager.start();
        getLogger().info("MiraFly v" + getPluginMeta().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.shutdown();
        else if (time != null) time.save();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (manager != null) manager.deactivate(event.getPlayer());
        if (time != null) time.save();
    }

    private void registerCommand(String name, FlyCommand executor) {
        PluginCommand command = getCommand(name);
        if (command == null) throw new IllegalStateException(name + " command missing from plugin.yml");
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
