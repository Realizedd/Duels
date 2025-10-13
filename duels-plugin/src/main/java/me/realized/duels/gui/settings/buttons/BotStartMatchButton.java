package me.realized.duels.gui.settings.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BotStartMatchButton extends BaseButton {

    public BotStartMatchButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Items.GREEN_PANE.clone()).name(plugin.getLang().getMessage("GUI.settings.buttons.send.name")).build());
    }

    @Override
    public void onClick(final Player player) {
        final Settings settings = settingManager.getSafely(player);

        if (settings.getTarget() == null) {
            settings.reset();
            player.closeInventory();
            return;
        }

        final Player target = Bukkit.getPlayer(settings.getTarget());

        if (target == null) {
            settings.reset();
            player.closeInventory();
            lang.sendMessage(player, "ERROR.player.no-longer-online");
            return;
        }

        if (!settings.isOwnInventory() && settings.getKit() == null) {
            player.closeInventory();

            KitImpl kit = settings.getArena().getKits().stream().findAny().orElse(null);

            if (kit == null) {
                lang.sendMessage(player, "ERROR.duel.mode-unselected");
                return;
            }

            settings.setKit(kit);
        }

        player.closeInventory();
        requestManager.send(player, target, settings);
    }
}
