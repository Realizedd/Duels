package me.realized.duels.api.event.kit;

import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Called when a {@link Kit} is removed.
 *
 * @see KitManager#remove(CommandSender, String)
 * @see Kit#isRemoved()
 */
public class KitRemoveEvent extends KitEvent {

    private static final HandlerList handlers = new HandlerList();

    public KitRemoveEvent(@Nullable final CommandSender source, @Nonnull final Kit kit) {
        super(source, kit);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
