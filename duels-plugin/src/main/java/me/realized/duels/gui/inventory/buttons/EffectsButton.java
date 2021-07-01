package me.realized.duels.gui.inventory.buttons;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.stream.Collectors;

public class EffectsButton extends BaseButton {

    public EffectsButton(final DuelsPlugin plugin, final Player player) {
        super(plugin, ItemBuilder
                .of(Items.WATER_BREATHING_POTION.clone())
                .name(plugin.getLang().getMessage("GUI.inventory-view.buttons.effects.name"))
                .lore(player.getActivePotionEffects().stream()
                        .map(effect -> plugin.getLang().getMessage("GUI.inventory-view.buttons.effects.lore-format",
                                "type", StringUtils.capitalize(effect.getType().getName().replace("_", " ").toLowerCase()),
                                "amplifier", StringUtil.toRoman(effect.getAmplifier() + 1),
                                "duration", (effect.getDuration() / 20))).collect(Collectors.toList()))
                .build());
        editMeta(meta -> meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS));
    }
}
