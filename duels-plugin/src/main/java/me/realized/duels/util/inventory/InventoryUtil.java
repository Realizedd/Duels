package me.realized.duels.util.inventory;

import com.google.common.collect.ObjectArrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class InventoryUtil {
    private static final String INVENTORY_IDENTIFIER = "INVENTORY";
    private static final String ARMOR_IDENTIFIER = "ARMOR";

    public static void addToMap(final PlayerInventory inventory, final Map<String, Map<Integer, ItemStack>> items) {
        final Map<Integer, ItemStack> contents = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            contents.put(i, item.clone());
        }

        items.put(INVENTORY_IDENTIFIER, contents);

        final Map<Integer, ItemStack> armorContents = new HashMap<>();

        for (int i = inventory.getArmorContents().length - 1; i >= 0; i--) {
            final ItemStack item = inventory.getArmorContents()[i];

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            armorContents.put(4 - i, inventory.getArmorContents()[i].clone());
        }

        items.put(ARMOR_IDENTIFIER, armorContents);
    }

    public static void fillFromMap(final PlayerInventory inventory, final Map<String, Map<Integer, ItemStack>> items) {
        final Map<Integer, ItemStack> inventoryItems = items.get(INVENTORY_IDENTIFIER);

        if (inventoryItems != null) {
            for (final Map.Entry<Integer, ItemStack> entry : inventoryItems.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue().clone());
            }
        }

        final Map<Integer, ItemStack> armorItems = items.get(ARMOR_IDENTIFIER);

        if (armorItems != null) {
            final ItemStack[] armor = new ItemStack[4];
            armorItems.forEach((slot, item) -> armor[4 - slot] = item.clone());
            inventory.setArmorContents(armor);
        }
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

    public static boolean addOrDrop(final Player player, final Collection<ItemStack> items) {
        if (items.isEmpty()) {
            return false;
        }

        final Map<Integer, ItemStack> result = player.getInventory().addItem(items.stream().filter(Objects::nonNull).toArray(ItemStack[]::new));

        if (!result.isEmpty()) {
            result.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }

        return true;
    }

    public static ItemStack getItemInHand(final Player player) {
        return player.getInventory().getItem(player.getInventory().getHeldItemSlot());
    }

    private InventoryUtil() {}

}
