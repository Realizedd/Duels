package me.realized.duels.api.queue;

import me.realized.duels.api.event.queue.QueueCreateEvent;
import me.realized.duels.api.event.queue.QueueJoinEvent;
import me.realized.duels.api.event.queue.QueueLeaveEvent;
import me.realized.duels.api.event.queue.QueueRemoveEvent;
import me.realized.duels.api.kit.Kit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents the QueueManager singleton used by Duels.
 *
 * @since 3.2.0
 */
public interface DQueueManager {

    /**
     * Gets a {@link DQueue} with the given kit and bet.
     *
     * @param kit Kit to check for match in the queues.
     * @param bet Bet to check for match in the queues.
     * @return DQueue with the given kit and bet if found, otherwise null.
     */
    @Nullable
    DQueue get(@Nullable final Kit kit, final int bet);


    /**
     * Gets a {@link DQueue} with the given {@link Player}.
     *
     * @param player Player to check if in queue
     * @return The queue player is in or null if not in queue
     */
    @Nullable
    DQueue get(@Nonnull final Player player);


    /**
     * Creates a new {@link DQueue}.
     * Note: Calls {@link QueueCreateEvent} on successful creation.
     *
     * @param kit Kit of the DQueue to create
     * @param bet Bet of the DQueue to create
     * @return The newly created DQueue or null if a queue with given kit and bet already exists
     */
    @Nullable
    DQueue create(@Nullable final CommandSender source, @Nullable final Kit kit, final int bet);


    /**
     * Calls {@link #create(CommandSender, Kit, int)} with source being null.
     *
     * @see #create(CommandSender, Kit, int)
     */
    @Nullable
    DQueue create(@Nullable final Kit kit, final int bet);


    /**
     * Removes a new {@link DQueue}.
     * Note: Calls {@link QueueRemoveEvent} on successful removal.
     *
     * @param kit Kit to check for match in the queues
     * @param bet Bet to check for match in the queues
     * @return The removed DQueue if found, otherwise null
     * @see DQueue#isRemoved()
     */
    @Nullable
    DQueue remove(@Nullable final CommandSender source, @Nullable final Kit kit, final int bet);


    /**
     * Calls {@link #remove(CommandSender, Kit, int)} with source being null.
     *
     * @see #remove(CommandSender, Kit, int)
     */
    @Nullable
    DQueue remove(@Nullable final Kit kit, final int bet);


    /**
     * Whether or not the {@link Player} is in a queue.
     *
     * @param player {@link Player} to check if in queue.
     * @return True if {@link Player} is in a queue. False otherwise.
     */
    boolean isInQueue(@Nonnull final Player player);


    /**
     * Adds the {@link Player} to the given {@link DQueue}.
     * Note: Calls {@link QueueJoinEvent}.
     *
     * @param player {@link Player} to add to {@link DQueue}.
     * @param queue  {@link DQueue} to add the {@link Player}.
     * @return True if {@link Player} was successfully queued. False otherwise.
     */
    boolean addToQueue(@Nonnull final Player player, @Nonnull final DQueue queue);


    /**
     * Removes the {@link Player} from the queue.
     * Note: Calls {@link QueueLeaveEvent} if {@link Player} was in a {@link DQueue}.
     *
     * @param player {@link Player} to remove from queue.
     * @return The {@link DQueue} that {@link Player} was in or null if {@link Player} was not in a queue.
     */
    @Nullable
    DQueue removeFromQueue(@Nonnull final Player player);


    /**
     * An UnmodifiableList of {@link DQueue}s that are currently loaded.
     *
     * @return Never-null UnmodifiableList of {@link DQueue}s that are currently loaded.
     */
    @Nonnull
    List<DQueue> getQueues();
}
