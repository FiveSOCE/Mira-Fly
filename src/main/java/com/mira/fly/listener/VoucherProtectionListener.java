package com.mira.fly.listener;

import com.mira.fly.service.FlyVoucherService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class VoucherProtectionListener implements Listener {
    private final FlyVoucherService vouchers;

    public VoucherProtectionListener(FlyVoucherService vouchers) {
        this.vouchers = vouchers;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (containsVoucher(event.getInventory())) event.setResult(null);
    }

    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        if (containsVoucher(event.getInventory())) event.setResult(null);
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (containsVoucher(event.getInventory())) event.setResult(null);
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (vouchers.isTagged(item)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareEnchant(PrepareItemEnchantEvent event) {
        if (vouchers.isTagged(event.getItem())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        if (vouchers.isTagged(event.getItem())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onResultClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!containsVoucher(top)) return;
        int raw = event.getRawSlot();
        boolean resultSlot = switch (top.getType()) {
            case ANVIL, GRINDSTONE -> raw == 2;
            case SMITHING -> raw == 3;
            case CRAFTING, WORKBENCH -> raw == 0;
            default -> false;
        };
        if (resultSlot) event.setCancelled(true);
    }

    private boolean containsVoucher(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) if (vouchers.isTagged(item)) return true;
        return false;
    }
}
