package me.realized.duels.api.event.queue;

import me.realized.duels.api.queue.DQueue;
import me.realized.duels.api.queue.DQueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Called when a player is leaving a {@link DQueue}.
 *
 * @see DQueueManager#removeFromQueue(Player)
 * @since 3.2.0
 */
public class QueueLeaveEvent extends QueueEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;

    public QueueLeaveEvent(@Nonnull final Player source, @Nonnull final DQueue queue) {
        super(source, queue);
        Objects.requireNonNull(source, "source");
        this.source = source;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * {@link Player} who is leaving the {@link DQueue}.
     *
     * @return Never-null {@link Player} who is leaving the {@link DQueue}.
     */
    @Nonnull
    @Override
    public Player getSource() {
        return source;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
