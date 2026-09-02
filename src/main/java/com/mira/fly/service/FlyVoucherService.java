package com.mira.fly.service;

import com.mira.fly.MiraFlyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

public final class FlyVoucherService {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private static final String VOUCHER_ID = "fly_5m";

    private final MiraFlyPlugin plugin;
    private final NamespacedKey idKey;
    private final NamespacedKey signatureKey;
    private byte[] signingSecret;

    public FlyVoucherService(MiraFlyPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "voucher_id");
        this.signatureKey = new NamespacedKey(plugin, "signature");
        ensureSigningSecret();
    }

    public ItemStack create(int amount) {
        Material material = Material.matchMaterial(plugin.getConfig().getString("voucher.material", "FEATHER"));
        if (material == null) material = Material.FEATHER;
        ItemStack item = new ItemStack(material, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        meta.displayName(component(plugin.getConfig().getString("voucher.name", "&b&l5 Minute Fly Voucher")));
        List<Component> lore = plugin.getConfig().getStringList("voucher.lore").stream().map(FlyVoucherService::component).toList();
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, VOUCHER_ID);
        meta.getPersistentDataContainer().set(signatureKey, PersistentDataType.STRING, signatureFor(VOUCHER_ID));
        item.setItemMeta(meta);
        return item;
    }

    public boolean isTagged(ItemStack item) {
        return item != null && !item.getType().isAir() && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(idKey, PersistentDataType.STRING);
    }

    public boolean isAuthentic(ItemStack item) {
        if (!isTagged(item)) return false;
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        String supplied = meta.getPersistentDataContainer().get(signatureKey, PersistentDataType.STRING);
        if (!VOUCHER_ID.equals(id) || supplied == null) return false;
        if (!MessageDigest.isEqual(supplied.getBytes(StandardCharsets.UTF_8), signatureFor(id).getBytes(StandardCharsets.UTF_8))) return false;
        return item.isSimilar(create(item.getAmount()));
    }

    public long secondsPerVoucher() {
        return Math.max(1L, plugin.getConfig().getLong("voucher.seconds", 300L));
    }

    private void ensureSigningSecret() {
        String configured = plugin.getConfig().getString("security.signing-secret", "").trim();
        if (configured.isEmpty()) {
            byte[] generated = new byte[32];
            new SecureRandom().nextBytes(generated);
            configured = Base64.getEncoder().encodeToString(generated);
            plugin.getConfig().set("security.signing-secret", configured);
            plugin.saveConfig();
            plugin.getLogger().info("Generated MiraFly voucher signing secret.");
        }
        signingSecret = Base64.getDecoder().decode(configured);
    }

    private String signatureFor(String id) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret, "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(id.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign MiraFly voucher", ex);
        }
    }

    public static Component component(String legacy) {
        return LEGACY.deserialize(legacy == null ? "" : legacy);
    }
}
