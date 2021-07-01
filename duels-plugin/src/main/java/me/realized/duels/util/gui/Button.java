package me.realized.duels.util.gui;

import lombok.Getter;
import lombok.Setter;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Button<P extends JavaPlugin> {

    protected final P plugin;
    @Getter
    @Setter
    private ItemStack displayed;

    public Button(final P plugin, final ItemStack displayed) {
        this.plugin = plugin;
        this.displayed = displayed;
    }

    protected void editMeta(final Consumer<ItemMeta> consumer) {
        final ItemMeta meta = getDisplayed().getItemMeta();
        consumer.accept(meta);
        getDisplayed().setItemMeta(meta);
    }

    protected void setDisplayName(final String name) {
        editMeta(meta -> meta.setDisplayName(StringUtil.color(name)));
    }

    protected void setLore(final List<String> lore) {
        editMeta(meta -> meta.setLore(StringUtil.color(lore)));
    }

    protected void setLore(final String... lore) {
        setLore(Arrays.asList(lore));
    }

    protected void setOwner(final String name) {
        editMeta(meta -> {
            if (Items.equals(Items.HEAD, displayed)) {
                ((SkullMeta) meta).setOwner(name);
            }
        });
    }

    protected void setGlow(final boolean glow) {
        // Normal golden apples do not have enchantment glint even with an enchantment applied, so we change the item type.
        if (displayed.getType().name().endsWith("GOLDEN_APPLE")) {
            final ItemStack item = glow ? Items.ENCHANTED_GOLDEN_APPLE.clone() : ItemBuilder.of(Material.GOLDEN_APPLE).build();
            item.setItemMeta(getDisplayed().getItemMeta());
            setDisplayed(item);
            return;
        }

        editMeta(meta -> {
            if (glow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.removeEnchant(Enchantment.DURABILITY);
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        });
    }


    public void update(final Player player) {
    }

    public void onClick(final Player player) {
    }
}
