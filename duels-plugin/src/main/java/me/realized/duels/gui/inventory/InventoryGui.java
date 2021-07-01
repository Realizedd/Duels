package me.realized.duels.gui.inventory;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.inventory.buttons.*;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.SinglePageGui;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryGui extends SinglePageGui<DuelsPlugin> {

    public InventoryGui(final DuelsPlugin plugin, final Player player) {
        super(plugin, plugin.getLang().getMessage("GUI.inventory-view.title", "name", player.getName()), 6);

        final ItemStack spacing = Items.GRAY_PANE.clone();
        Slots.run(0, 9, slot -> inventory.setItem(slot, spacing));
        set(4, new HeadButton(plugin, player));

        int potions = 0;
        int slot = 9;

        for (final ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (item.equals(Items.HEAL_SPLASH_POTION)) {
                    potions++;
                }

                inventory.setItem(slot, item.clone());
            }

            slot++;
        }

        slot = 48;

        for (final ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(slot, item.clone());
            }

            slot--;
        }

        inventory.setItem(49, spacing);
        set(50, new PotionCounterButton(plugin, potions));
        set(51, new EffectsButton(plugin, player));
        set(52, new HungerButton(plugin, player));
        set(53, new HealthButton(plugin, player));
    }
}
