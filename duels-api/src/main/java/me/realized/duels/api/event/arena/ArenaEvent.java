package me.realized.duels.api.event.arena;

import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.event.SourcedEvent;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents an event caused by a {@link Arena}.
 */
public abstract class ArenaEvent extends SourcedEvent {

    private final Arena arena;

    ArenaEvent(@Nullable final CommandSender source, @Nonnull final Arena arena) {
        super(source);
        Objects.requireNonNull(arena, "arena");
        this.arena = arena;
    }

    /**
     * {@link Arena} instance associated with this event.
     *
     * @return Never-null {@link Arena} instance associated with this event.
     */
    @Nonnull
    public Arena getArena() {
        return arena;
    }
}
