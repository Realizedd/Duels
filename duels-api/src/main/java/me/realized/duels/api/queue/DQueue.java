package me.realized.duels.api.queue;

import java.util.List;
import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Queue loaded on the server.
 */
public interface DQueue {

    /**
     * The {@link Kit} set for this {@link DQueue}.
     *
     * @return {@link Kit} set for this {@link DQueue} or null if no kit was set.
     */
    @Nullable
    Kit getKit();


    /**
     * The bet amount for this {@link DQueue}.
     *
     * @return Bet amount for this {@link DQueue} or 0 if no bet was specified.
     */
    double getBet();


    /**
     * Whether or not the given {@link Player} is in this {@link DQueue}.
     *
     * @param player Player to check if in this {@link DQueue}. Must not be null!
     * @return True if player is in this {@link DQueue}. False otherwise.
     */
    boolean isInQueue(@NotNull final Player player);


    /**
     * An UnmodifiableList of {@link Player}s in this queue.
     *
     * @return Never-null UnmodifiableList of {@link Player}s in this queue.
     */
    @NotNull
    List<Player> getQueuedPlayers();


    /**
     * Whether or not this {@link DQueue} has been removed.
     *
     * @return True if this {@link DQueue} has been removed. False otherwise.
     * @see DQueueManager#remove(Kit, double)
     */
    boolean isRemoved();
}
