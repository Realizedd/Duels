package me.realized.duels.api.event.spectate;

import me.realized.duels.api.spectate.Spectator;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * Called before a player stops spectating.
 */
public class SpectateEndEvent extends SpectateEvent {

    private static final HandlerList handlers = new HandlerList();

    public SpectateEndEvent(@Nonnull final Player source, @Nonnull final Spectator spectator) {
        super(source, spectator);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
