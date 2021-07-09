package me.realized.duels.api.kit;

import me.realized.duels.api.event.kit.KitEquipEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Represents an Kit loaded on the server.
 */
public interface Kit {

    /**
     * The name of this kit. Will not contain a dash character ("-").
     *
     * @return Never-null {@link String} that is the name of this kit.
     */
    @Nonnull
    String getName();


    /**
     * The item displayed in the kit selector gui.
     *
     * @return item displayed in the kit selector gui.
     */
    @Nonnull
    ItemStack getDisplayed();


    /**
     * Whether or not this kit requires a permission.
     * If usePermission is enabled, players must have the permission 'duels.kit.[name] (space in name replaced to dash)' to use the kit.
     *
     * @return True if usePermission is enabled for this kit. False otherwise.
     */
    boolean isUsePermission();


    /**
     * Enables or disables usePermission for this kit.
     *
     * @param usePermission True to enable usePermission. False otherwise.
     */
    void setUsePermission(final boolean usePermission);


    /**
     * Whether or not this kit has arenaSpecific enabled.
     * If arenaSpecific is enabled, only arenas with their name starting with this kit's name appended with an underscore will be available when playing with this kit.
     *
     * @return True if arenaSpecific is enabled. False otherwise.
     */
    boolean isArenaSpecific();


    /**
     * Enables or disables arenaSpecific for this kit.
     *
     * @param arenaSpecific True to enable arenaSpecific. False otherwise.
     */
    void setArenaSpecific(final boolean arenaSpecific);


    /**
     * Equips the {@link Player} with the contents of this kit.
     * Note: Calls {@link KitEquipEvent}.
     *
     * @param player {@link Player} to equip this kit.
     * @return True if {@link Player} has successfully equipped this kit. False otherwise.
     */
    boolean equip(@Nonnull final Player player);


    /**
     * Whether or not this {@link Kit} has been removed.
     *
     * @return True if this {@link Kit} has been removed. False otherwise.
     * @see KitManager#remove(CommandSender, String)
     * @since 3.2.0
     */
    boolean isRemoved();
}
