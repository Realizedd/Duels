package me.realized.duels.util.compat;

import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents players from keeping kit contents by removing items with the given key on click/interact/pickup
 */
public final class Tags {

    public static ItemStack setKey(final ItemStack item, final String key) {
        if (item == null) {
            return null;
        }
        try {
            return NBTEditor.set(item, "true", key);
        } catch (Exception ex) {
            ex.printStackTrace();
            return item;
        }
    }

    public static boolean hasKey(final ItemStack item, final String key) {
        if (item == null) {
            return false;
        }

        try {
            return NBTEditor.contains(item, key) && NBTEditor.getString(item, key).equalsIgnoreCase("true");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
