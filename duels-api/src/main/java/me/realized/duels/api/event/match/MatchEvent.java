package me.realized.duels.api.event.match;

import me.realized.duels.api.match.Match;
import org.bukkit.event.Event;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents an event caused by a {@link Match}.
 */
public abstract class MatchEvent extends Event {

    private final Match match;

    MatchEvent(@Nonnull final Match match) {
        Objects.requireNonNull(match, "match");
        this.match = match;
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
