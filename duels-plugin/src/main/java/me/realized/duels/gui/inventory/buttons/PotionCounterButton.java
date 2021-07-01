package me.realized.duels.gui.inventory.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.inventory.ItemFlag;

public class PotionCounterButton extends BaseButton {

    public PotionCounterButton(final DuelsPlugin plugin, final int count) {
        super(plugin, ItemBuilder
                .of(Items.HEAL_SPLASH_POTION.clone())
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.potion-counter.name", "potions", count))
                .build()
        );
        editMeta(meta -> meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS));
    }
}
