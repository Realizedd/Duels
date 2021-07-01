package me.realized.duels.util;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class PlayerUtil {

    private PlayerUtil() {
    }

    public static void reset(final Player player) {
        player.setFireTicks(0);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setItemOnCursor(null);

        final Inventory top = player.getOpenInventory().getTopInventory();

        if (top != null && top.getType() == InventoryType.CRAFTING) {
            top.clear();
        }

        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().clear();
        player.updateInventory();
    }
}
