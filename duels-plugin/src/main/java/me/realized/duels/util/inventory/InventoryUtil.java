package me.realized.duels.util.inventory;

import com.google.common.collect.ObjectArrays;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class InventoryUtil {

    private InventoryUtil() {
    }

    public static boolean hasItem(final Player player) {
        final PlayerInventory inventory = player.getInventory();

        for (final ItemStack item : ObjectArrays.concat(inventory.getArmorContents(), inventory.getContents(), ItemStack.class)) {
            if (item != null && item.getType() != Material.AIR) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack getItemInHand(final Player player) {
        return player.getInventory().getItem(player.getInventory().getHeldItemSlot());
    }
}
