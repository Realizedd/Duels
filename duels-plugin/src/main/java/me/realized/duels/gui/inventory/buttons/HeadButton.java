package me.realized.duels.gui.inventory.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class HeadButton extends BaseButton {

    public HeadButton(final DuelsPlugin plugin, final Player owner) {
        super(plugin, ItemBuilder
                .of(Items.HEAD.clone())
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.head.name", "name", owner.getName())).head(owner)
                .build()
        );
    }
}
