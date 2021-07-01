package me.realized.duels.gui.bind.buttons;

import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.gui.bind.BindGui;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class BindButton extends BaseButton {

    private final KitImpl kit;
    private final ArenaImpl arena;
    @Setter
    private BindGui gui;

    public BindButton(final DuelsPlugin plugin, final KitImpl kit, final ArenaImpl arena) {
        super(plugin, ItemBuilder.of(Items.EMPTY_MAP).build());
        this.kit = kit;
        this.arena = arena;
        setDisplayName(plugin.getLang().getMessage("GUI.bind.buttons.arena.name", "arena", arena.getName()));
        update();
    }

    private void update() {
        final boolean state = arena.isBound(kit);
        setGlow(state);

        String kits = StringUtils.join(arena.getKits().stream().map(KitImpl::getName).collect(Collectors.toList()), ", ");
        kits = kits.isEmpty() ? lang.getMessage("GENERAL.none") : kits;
        setLore(lang.getMessage("GUI.bind.buttons.arena.lore-" + (state ? "bound" : "not-bound"), "kits", kits).split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        arena.bind(kit);
        update();
        gui.calculatePages();
    }
}
