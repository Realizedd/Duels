package me.realized.duels.data;

import me.realized.duels.util.compat.ItemUtil;
import me.realized.duels.util.compat.Tags;
import org.bukkit.inventory.ItemStack;

public class ItemData {

    public static transient final String DUELS_ITEM_IDENTIFIER = "DuelsKitContent";

    private String serializedItem;

    // for Gson
    private ItemData() {
    }

    public ItemData(final ItemStack item) {
        this.serializedItem = ItemUtil.itemTo64(item);
    }

    public ItemStack toItemStack() {
        return Tags.setKey(ItemUtil.itemFrom64(serializedItem), DUELS_ITEM_IDENTIFIER);
    }
}
