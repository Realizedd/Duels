package me.realized.duels.api.match;

import java.util.List;
import java.util.Set;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an ongoing Match.
 */
public interface Match {


    /**
     * The {@link Arena} this {@link Match} is taking place in.
     *
     * @return {@link Arena} this {@link Match} is taking place in.
     */
    @NotNull
    Arena getArena();


    /**
     * The start of this match {@link Match} milliseconds.
     * Note: {@link System#currentTimeMillis()} subtracted by the result of this method will give the duration of the current {@link Match} in milliseconds.
     *
     * @return start of this match in milliseconds.
     */
    long getStart();


    /**
     * The {@link Kit} used in this {@link Match}.
     *
     * @return {@link Kit} used in this {@link Match} or null if players are using their own inventories.
     */
    @Nullable
    Kit getKit();


    /**
     * UnmodifiableList of ItemStacks the player has bet for this {@link Match}.
     *
     * @param player {@link Player} to get the list of bet items.
     * @return Never-null UnmodifiableList of ItemStacks the player has bet for this {@link Match}.
     */
    @NotNull
    List<ItemStack> getItems(@NotNull final Player player);


    /**
     * The bet amount for this {@link Match}.
     *
     * @return bet Bet amount for this {@link Match} or 0 if no bet was specified.
     */
    double getBet();


    /**
     * Whether or not this {@link Match} is finished.
     *
     * @return true if this {@link Match} has finished or false otherwise.
     * @since 3.4.1
     */
    boolean isFinished();


    /**
     * UnmodifiableSet of alive players in this {@link Match}.
     *
     * @return Never-null UnmodifiableSet of alive players in this {@link Match}.
     * @since 3.1.0
     */
    @NotNull
    Set<Player> getPlayers();


    /**
     * UnmodifiableSet of players who started this {@link Match}.
     * Note: This set includes players who are offline. If you keep a reference
     * to this match, all the player objects of those who started this match will
     * not be garbage-collected.
     *
     * @return Never-null UnmodifiableSet of players who started this {@link Match}.
     * @since 3.4.1
     */
    @NotNull
    Set<Player> getStartingPlayers();
}
