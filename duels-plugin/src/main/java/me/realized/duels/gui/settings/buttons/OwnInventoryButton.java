package me.realized.duels.gui.settings.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class OwnInventoryButton extends BaseButton {

    public OwnInventoryButton(final DuelsPlugin plugin) {
        super(plugin, ItemBuilder.of(Material.CHEST).name(plugin.getLang().getMessage("GUI.settings.buttons.use-own-inventory.name")).build());
    }

    @Override
    public void update(final Player player) {
        if (config.isOwnInventoryUsePermission() && !player.hasPermission(Permissions.OWN_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
            setLore(lang.getMessage("GUI.settings.buttons.use-own-inventory.lore-no-permission").split("\n"));
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String ownInventory = settings.isOwnInventory() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        final String lore = plugin.getLang().getMessage("GUI.settings.buttons.use-own-inventory.lore", "own_inventory", ownInventory);
        setLore(lore.split("\n"));
    }

    @Override
    public void onClick(final Player player) {
        if (config.isOwnInventoryUsePermission() && !player.hasPermission(Permissions.OWN_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.OWN_INVENTORY);
            return;
        }

        if (!config.isKitSelectingEnabled()) {
            lang.sendMessage(player, "ERROR.duel.mode-fixed");
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        settings.setOwnInventory(!settings.isOwnInventory());
        settings.gui.updateGui(player, settings);
    }
}