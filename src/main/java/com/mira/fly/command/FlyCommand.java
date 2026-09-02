package com.mira.fly.command;

import com.mira.fly.MiraFlyPlugin;
import com.mira.fly.service.FlyManager;
import com.mira.fly.service.FlyTimeService;
import com.mira.fly.service.FlyVoucherService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FlyCommand implements TabExecutor {
    private final MiraFlyPlugin plugin;
    private final FlyManager manager;
    private final FlyTimeService time;
    private final FlyVoucherService vouchers;

    public FlyCommand(MiraFlyPlugin plugin, FlyManager manager, FlyTimeService time, FlyVoucherService vouchers) {
        this.plugin = plugin;
        this.manager = manager;
        this.time = time;
        this.vouchers = vouchers;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "fly" -> handleFly(sender);
            case "flytime" -> handleFlyTime(sender);
            case "flyvoucher" -> handleFlyVoucher(sender, args);
            default -> { }
        }
        return true;
    }

    private void handleFly(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be used by a player.");
            return;
        }
        manager.toggle(player);
    }

    private void handleFlyTime(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be used by a player.");
            return;
        }
        String path = player.hasPermission("mirafly.permanent") ? "messages.permanent-time" : "messages.time";
        String message = plugin.getConfig().getString(path, "&bFly time: &f%time%")
                .replace("%time%", FlyTimeService.format(time.get(player.getUniqueId())));
        player.sendMessage(FlyVoucherService.component(message));
    }

    private void handleFlyVoucher(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mirafly.admin")) {
            sender.sendMessage(FlyVoucherService.component("&cYou do not have permission to administer MiraFly."));
            return;
        }
        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            sender.sendMessage(FlyVoucherService.component("&eUsage: /flyvoucher give <username> <amount>"));
            return;
        }

        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(FlyVoucherService.component("&cThat player is not online."));
            return;
        }

        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(FlyVoucherService.component("&cAmount must be a whole number."));
                return;
            }
        }
        if (amount < 1 || amount > 2304) {
            sender.sendMessage(FlyVoucherService.component("&cAmount must be between 1 and 2304."));
            return;
        }

        int remaining = amount;
        while (remaining > 0) {
            int stack = Math.min(64, remaining);
            ItemStack item = vouchers.create(stack);
            var overflow = target.getInventory().addItem(item);
            overflow.values().forEach(drop -> target.getWorld().dropItemNaturally(target.getLocation(), drop));
            remaining -= stack;
        }
        sender.sendMessage(FlyVoucherService.component("&aGave &f" + target.getName() + " &a" + amount + " fly voucher(s)."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("flyvoucher") || !sender.hasPermission("mirafly.admin")) return List.of();
        if (args.length == 1) return match(args[0], List.of("give"));
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return match(args[1], plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
        }
        return List.of();
    }

    private static List<String> match(String prefix, List<String> values) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String value : values) if (value.toLowerCase(Locale.ROOT).startsWith(lower)) out.add(value);
        return out;
    }
}
