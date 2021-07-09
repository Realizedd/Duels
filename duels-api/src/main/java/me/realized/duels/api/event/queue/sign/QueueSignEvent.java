package me.realized.duels.api.event.queue.sign;

import me.realized.duels.api.event.SourcedEvent;
import me.realized.duels.api.queue.sign.QueueSign;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents an event caused by a {@link QueueSign}.
 *
 * @since 3.2.0
 */
public abstract class QueueSignEvent extends SourcedEvent {

    private final Player source;
    private final QueueSign queueSign;

    QueueSignEvent(@Nonnull final Player source, @Nonnull final QueueSign queueSign) {
        super(source);
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(queueSign, "queueSign");
        this.source = source;
        this.queueSign = queueSign;
    }

    /**
     * {@link Player} who is the source of this event.
     *
     * @return Never-null {@link Player} who is the source of this event.
     */
    @Nonnull
    @Override
    public Player getSource() {
        return source;
    }

    /**
     * {@link QueueSign} instance associated with this event.
     *
     * @return Never-null {@link QueueSign} instance associated with this event.
     */
    @Nonnull
    public QueueSign getQueueSign() {
        return queueSign;
    }
}
