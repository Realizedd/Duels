package me.realized.duels.api.event.arena;

import me.realized.duels.api.arena.Arena;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Called when a new spawnpoint is set for an {@link Arena}.
 *
 * @see Arena#setPosition(Player, int, Location)
 */
public class ArenaSetPositionEvent extends ArenaEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private int pos;
    private Location location;
    private boolean cancelled;

    public ArenaSetPositionEvent(@Nullable final CommandSender source, @Nonnull final Arena arena, final int pos, @Nonnull final Location location) {
        super(source, arena);
        Objects.requireNonNull(location, "location");
        this.pos = pos;
        this.location = location;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * The position number of the spawnpoint set.
     *
     * @return position number of the spawnpoint set.
     */
    public int getPos() {
        return pos;
    }

    /**
     * Sets a new position number for the spawnpoint.
     *
     * @param pos New position number for the spawnpoint set.
     */
    public void setPos(final int pos) {
        this.pos = pos;
    }

    /**
     * The location of the spawnpoint set.
     *
     * @return location of the spawnpoint set.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets a new location for the spawnpoint.
     *
     * @param location New location for the spawnpoint set.
     */
    public void setLocation(final Location location) {
        this.location = location;
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
