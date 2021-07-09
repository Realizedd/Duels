package me.realized.duels.api.event.user;

import me.realized.duels.api.user.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Called when a new {@link User} is created.
 */
public class UserCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final User user;

    public UserCreateEvent(@Nonnull final User user) {
        Objects.requireNonNull(user, "user");
        this.user = user;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * The {@link User} that was created.
     *
     * @return Never-null {@link User} that was created.
     */
    @Nonnull
    public User getUser() {
        return user;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
