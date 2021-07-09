package me.realized.duels.api.event.arena;

import me.realized.duels.api.arena.Arena;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Called when an {@link Arena} is enabled or disabled.
 *
 * @see Arena#setDisabled(CommandSender, boolean)
 */
public class ArenaStateChangeEvent extends ArenaEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean disabled;
    private boolean cancelled;

    public ArenaStateChangeEvent(@Nullable final CommandSender source, @Nonnull final Arena arena, final boolean disabled) {
        super(source, arena);
        this.disabled = disabled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Whether or not the {@link Arena} is disabling.
     *
     * @return True if the {@link Arena} will be disabled. False otherwise.
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets a new state for the {@link Arena}.
     *
     * @param disabled True to disable the {@link Arena}. False to enable.
     */
    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
