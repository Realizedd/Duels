package me.realized.duels.api.event.queue;

import me.realized.duels.api.event.SourcedEvent;
import me.realized.duels.api.queue.DQueue;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents an event caused by a {@link DQueue}.
 *
 * @since 3.2.0
 */
public abstract class QueueEvent extends SourcedEvent {

    private final DQueue queue;

    QueueEvent(@Nullable final CommandSender source, @Nonnull final DQueue queue) {
        super(source);
        Objects.requireNonNull(queue, "queue");
        this.queue = queue;
    }

    /**
     * {@link DQueue} instance associated with this event.
     *
     * @return Never-null {@link DQueue} instance associated with this event.
     */
    @Nonnull
    public DQueue getQueue() {
        return queue;
    }
}
