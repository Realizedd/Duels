package me.realized.duels.api.event.queue.sign;

import me.realized.duels.api.queue.sign.QueueSign;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Called when a {@link QueueSign} is removed.
 *
 * @see QueueSign#isRemoved()
 * @since 3.2.0
 */
public class QueueSignRemoveEvent extends QueueSignEvent {

    private static final HandlerList handlers = new HandlerList();

    public QueueSignRemoveEvent(@Nonnull final Player source, @Nonnull final QueueSign queueSign) {
        super(source, queueSign);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
