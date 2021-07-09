package me.realized.duels.api.event.request;

import me.realized.duels.api.request.Request;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Called when a {@link Player} denies a {@link Request} from a {@link Player}.
 *
 * @since 3.2.1
 */
public class RequestDenyEvent extends RequestEvent {

    private static final HandlerList handlers = new HandlerList();

    /**
     * @param source  {@link Player} who is denying this {@link Request}.
     * @param target  {@link Player} who sent this {@link Request}.
     * @param request {@link Request} that is being handled.
     */
    public RequestDenyEvent(@Nonnull final Player source, @Nonnull final Player target, @Nonnull final Request request) {
        super(source, target, request);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
