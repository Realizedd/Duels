package me.realized.duels.kit;

import java.util.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.event.kit.KitEquipEvent;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class KitImpl extends BaseButton implements Kit {

    @Getter
    private final String name;
    @Getter
    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();
    @Getter
    private boolean usePermission;
    @Getter
    private boolean arenaSpecific;
    @Getter
    private Set<Characteristic> characteristics;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    public KitImpl(final DuelsPlugin plugin, final String name, final ItemStack displayed, final boolean usePermission,
                   final boolean arenaSpecific, final Set<Characteristic> characteristics) {
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
        InventoryUtil.addToMap(inventory, items);
    }

    // Never-null since if null item is passed to the constructor, a default item is passed to super
    @NotNull
    public ItemStack getDisplayed() {
        return super.getDisplayed();
    }

    @Override
    public void setDisplayed(final ItemStack displayed) {
        super.setDisplayed(displayed);
        kitManager.saveKits();
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
        kitManager.saveKits();
    }

    @Override
    public void setUsePermission(final boolean usePermission) {
        this.usePermission = usePermission;
        kitManager.saveKits();
    }

    @Override
    public void setArenaSpecific(final boolean arenaSpecific) {
        this.arenaSpecific = arenaSpecific;
        kitManager.saveKits();
    }

    @Override
    public boolean equip(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        final KitEquipEvent event = new KitEquipEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        InventoryUtil.fillFromMap(player.getInventory(), items);
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

        // Reset arena selection if this kit is incompatible with selected arena
        if (settings.getArena() != null && !arenaManager.isSelectable(this, settings.getArena())) {
            settings.setArena(null);
        }

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
        COMBO,
        HUNGER,
        PLACE,
        BREAK,
        BOXING;
    }
}
