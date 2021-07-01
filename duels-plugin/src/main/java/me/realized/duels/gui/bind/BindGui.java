package me.realized.duels.gui.bind;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.gui.bind.buttons.BindButton;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.MultiPageGui;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;

import java.util.stream.Collectors;

public class BindGui extends MultiPageGui<DuelsPlugin> {

    public BindGui(final DuelsPlugin plugin, final KitImpl kit) {
        super(plugin, plugin.getLang().getMessage("GUI.bind.title", "kit", kit.getName()), plugin.getConfiguration().getArenaSelectorRows(),
                plugin.getArenaManager().getArenasImpl().stream().map(arena -> new BindButton(plugin, kit, arena)).collect(Collectors.toList()));

        final Config config = plugin.getConfiguration();
        final Lang lang = plugin.getLang();
        setSpaceFiller(Items.from(config.getArenaSelectorFillerType(), config.getArenaSelectorFillerData()));
        setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.arena-selector.buttons.previous-page.name")).build());
        setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.arena-selector.buttons.next-page.name")).build());
        setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.arena-selector.buttons.empty.name")).build());
        // Change design to remove this loop in the future
        getButtons().forEach(button -> ((BindButton) button).setGui(this));
        calculatePages();
    }
}
