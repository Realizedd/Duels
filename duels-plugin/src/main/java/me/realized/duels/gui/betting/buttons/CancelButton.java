package me.realized.duels.gui.betting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

public class CancelButton extends BaseButton {

    public CancelButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder
                .of(Items.RED_PANE.clone())
                .name(plugin.getLang().getMessage("GUI.item-betting.buttons.cancel.name"))
                .lore(plugin.getLang().getMessage("GUI.item-betting.buttons.cancel.lore").split("\n"))
                .build()
        );
    }

    @Override
    public void onClick(final Player player) {
        player.closeInventory();
    }
}
