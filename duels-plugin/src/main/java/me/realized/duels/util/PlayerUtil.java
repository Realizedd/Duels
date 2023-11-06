package me.realized.duels.util;

import me.realized.duels.util.compat.CompatUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class PlayerUtil {

    private static final double DEFAULT_MAX_HEALTH = 20.0D;
    private static final float DEFAULT_EXHAUSTION = 0.0F;
    private static final float DEFAULT_SATURATION = 5.0F;
    private static final int DEFAULT_MAX_FOOD_LEVEL = 20;

    public static double getMaxHealth(final Player player) {
        if (CompatUtil.isPre1_9()) {
            return player.getMaxHealth();
        } else {
            final AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (attribute == null) {
                return DEFAULT_MAX_HEALTH;
            }

            return attribute.getValue();
        }
    }

    private static void setMaxHealth(final Player player) {
        player.setHealth(getMaxHealth(player));
    }

    public static void reset(final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DuelsPlugin.getInstance(), () -> {
            player.closeInventory();

            player.setFireTicks(0);
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            setMaxHealth(player);
            player.setFoodLevel(DEFAULT_MAX_FOOD_LEVEL);
            player.setItemOnCursor(null);

            final Inventory top = player.getOpenInventory().getTopInventory();

            if (top != null && top.getType() == InventoryType.CRAFTING) {
                top.clear();
            }

            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().clear();
            player.updateInventory();
        }, 2L);
    }

    private PlayerUtil() {}
}
