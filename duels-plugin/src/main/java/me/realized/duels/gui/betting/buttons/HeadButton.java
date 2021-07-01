package me.realized.duels.gui.betting.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HeadButton extends BaseButton {

    private final UUID owner;

    public HeadButton(final DuelsPlugin plugin, final Player owner) {
        super(plugin, ItemBuilder
                .of(Items.HEAD.clone())
                .name(plugin.getLang().getMessage("GUI.item-betting.buttons.head.name", "name", owner.getName()))
                .build()
        );
        this.owner = owner.getUniqueId();
    }

    @Override
    public void update(final Player player) {
        if (player.getUniqueId().equals(owner)) {
            setOwner(player.getName());
            setDisplayName(lang.getMessage("GUI.item-betting.buttons.head.name", "name", player.getName()));
        }
    }
}
