package me.realized.duels.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

/**
 * Represents an event that may have a source.
 */
public abstract class SourcedEvent extends Event {

    private final CommandSender source;

    protected SourcedEvent(@Nullable final CommandSender source) {
        this.source = source;
    }

    /**
     * Source of this event. May be null!
     *
     * @return {@link CommandSender} that is the source of this event or null.
     */
    @Nullable
    public CommandSender getSource() {
        return source;
    }


    /**
     * Whether or not this event has a source specified.
     *
     * @return True if there was a source specified. False otherwise.
     */
    public boolean hasSource() {
        return getSource() != null;
    }
}
