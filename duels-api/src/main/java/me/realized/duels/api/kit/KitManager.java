package me.realized.duels.api.kit;

import me.realized.duels.api.event.kit.KitCreateEvent;
import me.realized.duels.api.event.kit.KitRemoveEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents the KitManager singleton used by Duels.
 */
public interface KitManager {

    /**
     * Attempts to find an {@link Kit} instance associated with the given name.
     *
     * @param name Name to search through the loaded kits. Should not be null!
     * @return {@link Kit} instance that has a name matching with the given name or null if not exists.
     */
    @Nullable
    Kit get(@Nonnull final String name);


    /**
     * Creates a kit with given name and {@link Player}'s inventory contents.
     * Note: Calls {@link KitCreateEvent} on successful creation.
     *
     * @param creator {@link Player} who is the creator of this kit.
     * @param name    Name of the newly created {@link Kit}. Requires to be alphanumeric (underscore is allowed).
     * @return The newly created {@link Kit} or null if a kit with given name already exists.
     */
    @Nullable
    Kit create(@Nonnull final Player creator, @Nonnull final String name);


    /**
     * Removes a kit with given name.
     * Note: Calls {@link KitRemoveEvent} on successful removal.
     *
     * @param source {@link CommandSender} who is the source of this call.
     * @param name   Name of the kit to remove.
     * @return The removed {@link Kit} or null if no {@link Kit} was found with the given name.
     */
    @Nullable
    Kit remove(@Nullable CommandSender source, @Nonnull final String name);


    /**
     * Calls {@link #remove(CommandSender, String)} with source being null.
     *
     * @see #remove(CommandSender, String)
     */
    @Nullable
    Kit remove(@Nonnull final String name);


    /**
     * An UnmodifiableList of {@link Kit}s that are currently loaded.
     *
     * @return Never-null UnmodifiableList of {@link Kit}s that are currently loaded.
     * @since 3.1.0
     */
    @Nonnull
    List<Kit> getKits();
}
