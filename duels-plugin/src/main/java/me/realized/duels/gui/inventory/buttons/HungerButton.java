package me.realized.duels.gui.inventory.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class HungerButton extends BaseButton {

    public HungerButton(final DuelsPlugin plugin, final Player player) {
        super(plugin, ItemBuilder
                .of(Material.COOKED_BEEF)
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.hunger.name", "hunger", player.getFoodLevel()))
                .build()
        );
    }
}
