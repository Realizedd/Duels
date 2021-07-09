package me.realized.duels.api.event.request;

import me.realized.duels.api.event.SourcedEvent;
import me.realized.duels.api.request.Request;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents an event caused by a {@link Request}.
 *
 * @since 3.2.1
 */
public abstract class RequestEvent extends SourcedEvent {

    private final Player source, target;
    private final Request request;

    RequestEvent(@Nonnull final Player source, @Nonnull final Player target, @Nonnull final Request request) {
        super(source);
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(request, "request");
        this.source = source;
        this.target = target;
        this.request = request;
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
     * {@link Player} who is the target of this event.
     *
     * @return Never-null {@link Player} who is the target of this event.
     */
    @Nonnull
    public Player getTarget() {
        return target;
    }

    /**
     * {@link Request} instance associated with this event.
     *
     * @return Never-null {@link Request} instance associated with this event.
     */
    @Nonnull
    public Request getRequest() {
        return request;
    }
}
