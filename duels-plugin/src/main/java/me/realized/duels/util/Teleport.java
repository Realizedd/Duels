package me.realized.duels.util;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.metadata.MetadataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.function.Consumer;

/**
 * Handles force teleporting of players.
 */
public final class Teleport implements Loadable, Listener {

    public static final String METADATA_KEY = "Duels-Teleport";

    private final DuelsPlugin plugin;

    public Teleport(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {
        // Late-register the listener to override previously registered listeners
        plugin.doSyncAfter(() -> plugin.registerListener(this), 1L);
    }

    @Override
    public void handleUnload() {
    }

    /**
     * Attempts to force-teleport a player by storing a metadata value in the player before teleportation
     * and uncancelling the teleport by the player in a MONITOR-priority listener if cancelled by other plugins.
     *
     * @param player      Player to force-teleport to a location
     * @param location    Location to force-teleport the player
     * @param failHandler Called when teleportation has failed -- being when Player#teleport returns false.
     */
    public void tryTeleport(final Player player, final Location location, final Consumer<Player> failHandler) {
        if (location == null || location.getWorld() == null) {
            Log.warn(this, "Could not teleport " + player.getName() + "! Location is null");

            if (failHandler != null) {
                failHandler.accept(player);
            }
            return;
        }

        //TODO: CMI SETBACKLOCATION

        final Chunk chunk = location.getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        MetadataUtil.put(plugin, player, METADATA_KEY, location.clone());

        if (!player.teleport(location)) {
            Log.warn(this, "Could not teleport " + player.getName() + "! Player is dead or is vehicle");

            if (failHandler != null) {
                failHandler.accept(player);
            }
        }

        if (plugin.isDisabling()) {
            return;
        }

        plugin.doSyncAfter(() -> Bukkit.getOnlinePlayers().forEach(online -> {
            if (player.canSee(online) && online.canSee(player)) {
                player.hidePlayer(online);
                online.hidePlayer(player);
                player.showPlayer(online);
                online.showPlayer(player);
            }
        }), 1L);
    }

    /**
     * Calls {@link #tryTeleport(Player, Location, Consumer)} with a null FailHandler.
     *
     * @see #tryTeleport(Player, Location, Consumer)
     */
    public void tryTeleport(final Player player, final Location location) {
        tryTeleport(player, location, null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final Object value = MetadataUtil.removeAndGet(plugin, player, METADATA_KEY);

        // Only handle the case where teleport is cancelled and player has force teleport metadata value
        if (!event.isCancelled() || value == null) {
            return;
        }

        event.setCancelled(false);
        event.setTo((Location) value);
    }
}
