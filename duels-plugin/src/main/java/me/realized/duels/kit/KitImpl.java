package me.realized.duels.kit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.event.kit.KitEquipEvent;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import javax.annotation.Nonnull;
import java.util.*;

public class KitImpl extends BaseButton implements Kit {

    @Getter
    private final String name;
    @Getter
    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();
    @Getter
    @Setter
    private boolean usePermission;
    @Getter
    @Setter
    private boolean arenaSpecific;
    @Getter
    private Set<Characteristic> characteristics;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    public KitImpl(final DuelsPlugin plugin, final String name, final ItemStack displayed, final boolean usePermission, final boolean arenaSpecific,
                   final Set<Characteristic> characteristics) {
        super(plugin, displayed != null ? displayed : ItemBuilder
                .of(Material.DIAMOND_SWORD)
                .name("&7&l" + name)
                .lore("&aClick to send", "&aa duel request", "&awith this kit!")
                .build());
        this.name = name;
        this.usePermission = usePermission;
        this.arenaSpecific = arenaSpecific;
        this.characteristics = characteristics;
    }

    public KitImpl(final DuelsPlugin plugin, final String name, final PlayerInventory inventory) {
        this(plugin, name, null, false, false, new HashSet<>());

        final Map<Integer, ItemStack> contents = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            contents.put(i, item.clone());
        }

        items.put("INVENTORY", contents);

        final Map<Integer, ItemStack> armorContents = new HashMap<>();

        for (int i = inventory.getArmorContents().length - 1; i >= 0; i--) {
            final ItemStack item = inventory.getArmorContents()[i];

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            armorContents.put(4 - i, inventory.getArmorContents()[i].clone());
        }

        items.put("ARMOR", armorContents);
    }

    // Never-null since if null item is passed to the constructor, a default item is passed to super
    @Nonnull
    public ItemStack getDisplayed() {
        return super.getDisplayed();
    }

    public boolean hasCharacteristic(final Characteristic characteristic) {
        return characteristics.contains(characteristic);
    }

    public void toggleCharacteristic(final Characteristic characteristic) {
        if (hasCharacteristic(characteristic)) {
            characteristics.remove(characteristic);
        } else {
            characteristics.add(characteristic);
        }
    }

    @Override
    public boolean equip(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        final KitEquipEvent event = new KitEquipEvent(player, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        for (final Map.Entry<Integer, ItemStack> entry : items.get("INVENTORY").entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue().clone());
        }

        final ItemStack[] armor = new ItemStack[4];
        items.get("ARMOR").forEach((slot, item) -> armor[4 - slot] = item.clone());
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
        return true;
    }

    @Override
    public void onClick(final Player player) {
        final String permission = String.format(Permissions.KIT, name.replace(" ", "-").toLowerCase());

        if (usePermission && !player.hasPermission(Permissions.KIT_ALL) && !player.hasPermission(permission)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", permission);
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        settings.setKit(this);
        settings.openGui(player);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        final KitImpl kit = (KitImpl) other;
        return Objects.equals(name, kit.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public enum Characteristic {

        SOUP,
        SUMO,
        UHC,
        COMBO;
    }
}
