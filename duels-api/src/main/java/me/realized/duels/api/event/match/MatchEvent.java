package me.realized.duels.api.event.match;

import java.util.Objects;
import me.realized.duels.api.match.Match;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event caused by a {@link Match}.
 */
public abstract class MatchEvent extends Event {

    protected static final HandlerList handlers = new HandlerList();
    private final Match match;

    MatchEvent(@NotNull final Match match) {
        Objects.requireNonNull(match, "match");
        this.match = match;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * {@link Match} instance associated with this event.
     *
     * @return Never-null {@link Match} instance associated with this event.
     */
    public Match getMatch() {
        return match;
    }
}
