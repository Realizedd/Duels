package me.realized.duels.api.event.kit;

import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Called when a {@link Kit} is created.
 *
 * @see KitManager#create(Player, String)
 */
public class KitCreateEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;

    public KitCreateEvent(@Nonnull final Player source, @Nonnull final Kit kit) {
        super(source, kit);
        Objects.requireNonNull(source, "source");
        this.source = source;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Nonnull
    @Override
    public Player getSource() {
        return source;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
