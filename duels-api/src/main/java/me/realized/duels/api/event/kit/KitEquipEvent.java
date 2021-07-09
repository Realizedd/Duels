package me.realized.duels.api.event.kit;

import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Called when a {@link Player} equips a {@link Kit}.
 *
 * @see Kit#equip(Player)
 */
public class KitEquipEvent extends KitEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player source;
    private boolean cancelled;

    public KitEquipEvent(@Nonnull final Player source, @Nonnull final Kit kit) {
        super(source, kit);
        Objects.requireNonNull(source, "source");
        this.source = source;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * {@link Player} who is equipping the {@link Kit}.
     *
     * @return Never-null {@link Player} who is equipping the {@link Kit}.
     */
    @Nonnull
    @Override
    public Player getSource() {
        return source;
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
