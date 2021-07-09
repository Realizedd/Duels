package me.realized.duels.api.queue.sign;

import org.bukkit.block.Sign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents the QueueSignManager singleton used by Duels.
 *
 * @since 3.2.0
 */
public interface QueueSignManager {

    /**
     * Gets a {@link QueueSign} that is associated with the given {@link Sign}.
     *
     * @param sign {@link Sign} to match in the loaded queue signs.
     * @return {@link QueueSign} associated with the {@link Sign} or null if not found.
     */
    @Nullable
    QueueSign get(@Nonnull final Sign sign);


    /**
     * An UnmodifiableList of {@link QueueSign}s that are currently loaded.
     *
     * @return Never-null UnmodifiableList of {@link QueueSign}s that are currently loaded.
     */
    @Nonnull
    List<QueueSign> getQueueSigns();
}
