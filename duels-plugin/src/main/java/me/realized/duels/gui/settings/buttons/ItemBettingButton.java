package me.realized.duels.gui.settings.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ItemBettingButton extends BaseButton {

    public ItemBettingButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.DIAMOND).name(plugin.getLang().getMessage("GUI.settings.buttons.item-betting.name")).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang.getMessage("GUI.settings.buttons.item-betting.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        final String lore = plugin.getLang().getMessage("GUI.settings.buttons.item-betting.lore", "item_betting", itemBetting);
        setLore(lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ITEM_BETTING);
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        settings.setItemBetting(!settings.isItemBetting());
        settings.gui.updateGui(player, settings);
    }
}
