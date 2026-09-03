package com.mira.fly.listener;

import com.mira.fly.MiraFlyPlugin;
import com.mira.fly.service.FlyTimeService;
import com.mira.fly.service.FlyVoucherService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FlyVoucherListener implements Listener {
    private final MiraFlyPlugin plugin;
    private final FlyVoucherService vouchers;
    private final FlyTimeService time;
    private final Set<UUID> redeeming = new HashSet<>();

    public FlyVoucherListener(MiraFlyPlugin plugin, FlyVoucherService vouchers, FlyTimeService time) {
        this.plugin = plugin;
        this.vouchers = vouchers;
        this.time = time;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() == null) return;
        ItemStack item = event.getItem();
        if (!vouchers.isTagged(item)) return;

        event.setCancelled(true);
        if (!vouchers.isAuthentic(item)) {
            send(event.getPlayer(), "invalid-voucher");
            return;
        }

        UUID uuid = event.getPlayer().getUniqueId();
        if (!redeeming.add(uuid)) return;
        try {
            long updated = time.add(uuid, vouchers.secondsPerVoucher());
            String message = plugin.getConfig().getString("messages.redeemed", "&aFly voucher redeemed. Total: &f%time%")
                    .replace("%time%", FlyTimeService.format(updated));
            event.getPlayer().sendMessage(FlyVoucherService.chat(message));
            consumeOne(event.getPlayer(), event.getHand());
        } finally {
            plugin.getServer().getScheduler().runTask(plugin, () -> redeeming.remove(uuid));
        }
    }

    private void consumeOne(org.bukkit.entity.Player player, EquipmentSlot hand) {
        ItemStack held = hand == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        if (!vouchers.isAuthentic(held)) return;
        if (held.getAmount() <= 1) {
            if (hand == EquipmentSlot.HAND) player.getInventory().setItemInMainHand(null);
            else player.getInventory().setItemInOffHand(null);
        } else held.setAmount(held.getAmount() - 1);
    }

    private void send(org.bukkit.entity.Player player, String key) {
        String message = plugin.getConfig().getString("messages." + key, "");
        if (!message.isBlank()) player.sendMessage(FlyVoucherService.chat(message));
    }
}
