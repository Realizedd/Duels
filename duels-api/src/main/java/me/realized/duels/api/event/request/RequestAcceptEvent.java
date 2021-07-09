package me.realized.duels.api.event.request;

import me.realized.duels.api.request.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Called when a {@link Player} accepts a {@link Request} from a {@link Player}.
 *
 * @since 3.2.1
 */
public class RequestAcceptEvent extends RequestEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    /**
     * @param source  {@link Player} who is accepting this {@link Request}.
     * @param target  {@link Player} who sent this {@link Request}.
     * @param request {@link Request} that is being handled.
     */
    public RequestAcceptEvent(@Nonnull final Player source, @Nonnull final Player target, @Nonnull final Request request) {
        super(source, target, request);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Whether or not this event has been cancelled.
     *
     * @return True if this event has been cancelled. False otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Whether or not to cancel this event.
     * When cancelled, the request will not be removed and remain as unhandled.
     *
     * @param cancelled True to cancel this event.
     */
    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
