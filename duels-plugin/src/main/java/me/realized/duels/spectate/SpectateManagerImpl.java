package me.realized.duels.spectate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.event.spectate.SpectateEndEvent;
import me.realized.duels.api.event.spectate.SpectateStartEvent;
import me.realized.duels.api.spectate.SpectateManager;
import me.realized.duels.api.spectate.Spectator;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.arena.MatchImpl;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.hook.hooks.MyPetHook;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.Teleport;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class SpectateManagerImpl implements Loadable, SpectateManager {

    // The location of the stop spec item in player's hotbar.
    private static final int STOP_SPEC_ITEM_SLOT = 8;

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final ArenaManagerImpl arenaManager;
    private final PlayerInfoManager playerManager;

    // Map UUID to a spectator
    private final Map<UUID, SpectatorImpl> spectators = Maps.newHashMap();
    // Map arena to a collection of spectators
    private final Multimap<Arena, SpectatorImpl> arenas = HashMultimap.create();

    private Teleport teleport;
    private MyPetHook myPet;

    public SpectateManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();

        plugin.getServer().getPluginManager().registerEvents(new SpectateListener(), plugin);
    }

    @Override
    public void handleLoad() {
        // Late-init since SpectateManager is loaded before below variables are loaded
        this.teleport = plugin.getTeleport();
        this.myPet = plugin.getHookManager().getHook(MyPetHook.class);
    }

    @Override
    public void handleUnload() {
        spectators.clear();
    }

    @Nullable
    @Override
    public SpectatorImpl get(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        return spectators.get(player.getUniqueId());
    }

    @Override
    public boolean isSpectating(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player) != null;
    }

    @Nonnull
    @Override
    public Result startSpectating(@Nonnull final Player player, @Nonnull final Player target) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(target, "target");

        if (isSpectating(player)) {
            return Result.ALREADY_SPECTATING;
        }

        if (plugin.getQueueManager().isInQueue(player)) {
            return Result.IN_QUEUE;
        }

        if (arenaManager.isInMatch(player)) {
            return Result.IN_MATCH;
        }

        final ArenaImpl arena = arenaManager.get(target);

        if (arena == null) {
            return Result.TARGET_NOT_IN_MATCH;
        }

        final SpectatorImpl spectator = new SpectatorImpl(player, target, arena);
        final SpectateStartEvent event = new SpectateStartEvent(player, spectator);
        plugin.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return Result.EVENT_CANCELLED;
        }

        final MatchImpl match = arena.getMatch();

        // Hide from players in match
        if (match != null) {
            match.getAllPlayers()
                    .stream()
                    .filter(arenaPlayer -> arenaPlayer.isOnline() && arenaPlayer.canSee(player))
                    .forEach(arenaPlayer -> arenaPlayer.hidePlayer(player));
        }

        // Remove pet before teleport
        if (myPet != null) {
            myPet.removePet(player);
        }

        // Cache current player state to return to after spectating is over
        playerManager.put(player, new PlayerInfo(player, !config.isSpecRequiresClearedInventory()));
        PlayerUtil.reset(player);

        // Teleport before putting in map prevent teleport being cancelled by SpectateListener
        teleport.tryTeleport(player, target.getLocation().clone().add(0, 2, 0));
        spectators.put(player.getUniqueId(), spectator);
        arenas.put(arena, spectator);
        player.getInventory().setItem(STOP_SPEC_ITEM_SLOT, ItemBuilder.of(Material.PAPER).name(lang.getMessage("SPECTATE.stop-item.name")).build());
        player.setAllowFlight(true);
        player.setFlying(true);

        // Ignore collision for spectators. This allows projectiles, such as arrows, to pass through.
        player.setCollidable(false);

        if (config.isSpecAddInvisibilityEffect()) {
            // Particles parameter (last parameter) does not exist in 1.7
            final PotionEffect effect = CompatUtil.isPre1_8() ?
                    new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false) :
                    new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false);
            player.addPotionEffect(effect);
        }

        // Broadcast to the arena that player has begun spectating if player does not have the SPEC_ANON permission.
        if (!player.hasPermission(Permissions.SPEC_ANON)) {
            arena.getMatch().getAllPlayers().forEach(matchPlayer -> lang.sendMessage(matchPlayer, "SPECTATE.arena-broadcast", "name", player.getName()));
        }

        return Result.SUCCESS;
    }

    /**
     * Puts player out of spectator mode.
     *
     * @param player    Player to put out of spectator mode
     * @param spectator {@link SpectatorImpl} instance associated to this player
     */
    public void stopSpectating(final Player player, final SpectatorImpl spectator) {
        spectators.remove(player.getUniqueId());
        arenas.remove(spectator.getArena(), spectator);

        PlayerUtil.reset(player);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setCollidable(true);

        final PlayerInfo info = playerManager.removeAndGet(player);

        if (info != null) {
            teleport.tryTeleport(player, info.getLocation(), failed -> {
                failed.setHealth(0);
                failed.sendMessage(StringUtil.color("&cTeleportation failed! You were killed to prevent staying in the arena."));
            });
            info.restore(player);
        } else {
            teleport.tryTeleport(player, playerManager.getLobby());
        }

        final MatchImpl match = spectator.getArena().getMatch();

        // Show to players in match
        if (match != null) {
            match.getAllPlayers()
                    .stream()
                    .filter(Player::isOnline)
                    .forEach(matchPlayer -> matchPlayer.showPlayer(player));
        }

        final SpectateEndEvent event = new SpectateEndEvent(player, spectator);
        plugin.getServer().getPluginManager().callEvent(event);
    }

    /**
     * Puts player out of spectator mode.
     *
     * @see #stopSpectating(Player, SpectatorImpl)
     */
    @Override
    public void stopSpectating(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        stopSpectating(player, get(player));
    }

    /**
     * Puts all spectators of the given {@link ArenaImpl} out of spectator mode.
     *
     * @param arena {@link ArenaImpl} to end spectating
     */
    public void stopSpectating(final ArenaImpl arena) {
        final Collection<SpectatorImpl> spectators = arenas.asMap().remove(arena);

        if (spectators == null || spectators.isEmpty()) {
            return;
        }

        spectators.forEach(spectator -> {
            final Player player = plugin.getServer().getPlayer(spectator.getUuid());

            if (player == null) {
                return;
            }

            stopSpectating(player, spectator);
            lang.sendMessage(player, "SPECTATE.match-end");
        });
    }

    @Nonnull
    @Override
    public List<Spectator> getSpectators(@Nonnull final Arena arena) {
        Objects.requireNonNull(arena, "arena");
        return Collections.unmodifiableList(Lists.newArrayList(getSpectatorsImpl(arena)));
    }

    public Collection<SpectatorImpl> getSpectatorsImpl(final Arena arena) {
        return arenas.asMap().getOrDefault(arena, Collections.emptyList());
    }

    public Collection<Player> getAllSpectators() {
        return spectators.values()
                .stream()
                .map(spectator -> plugin.getServer().getPlayer(spectator.getUuid()))
                .collect(Collectors.toList());
    }

    private class SpectateListener implements Listener {

        @EventHandler
        public void on(final PlayerInteractEvent event) {
            final Player player = event.getPlayer();
            final SpectatorImpl spectator = get(player);

            if (spectator == null) {
                return;
            }

            if (config.isSpecPreventBlockInteract()) {
                event.setCancelled(true);
            }

            if (event.getAction() == Action.PHYSICAL) {
                return;
            }

            final ItemStack held = InventoryUtil.getItemInHand(player);

            if (held == null || held.getType() != Material.PAPER) {
                return;
            }

            stopSpectating(player, spectator);
            lang.sendMessage(player, "COMMAND.spectate.stop-spectate", "name", spectator.getTargetName());
        }

        @EventHandler
        public void on(final PlayerQuitEvent event) {
            final Player player = event.getPlayer();
            final SpectatorImpl spectator = get(player);

            if (spectator == null) {
                return;
            }

            stopSpectating(player, spectator);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerCommandPreprocessEvent event) {
            final Player player = event.getPlayer();

            if (!isSpectating(player)) {
                return;
            }

            final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

            if (command.equalsIgnoreCase("spectate") || command.equalsIgnoreCase("spec") || config.getSpecWhitelistedCommands().contains(command)) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "SPECTATE.prevent.command", "command", event.getMessage());
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerTeleportEvent event) {
            final Player player = event.getPlayer();
            final SpectatorImpl spectator = get(player);

            if (spectator == null) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "SPECTATE.prevent.teleportation");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerDropItemEvent event) {
            final Player player = event.getPlayer();

            if (!isSpectating(player)) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "SPECTATE.prevent.item-drop");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(PlayerPickupItemEvent event) {
            if (!isSpectating(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final InventoryClickEvent event) {
            if (!isSpectating((Player) event.getWhoClicked())) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler
        public void on(final InventoryDragEvent event) {
            if (!isSpectating((Player) event.getWhoClicked())) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageByEntityEvent event) {
            final Player player;

            if (event.getDamager() instanceof Player) {
                player = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player) {
                player = (Player) ((Projectile) event.getDamager()).getShooter();
            } else {
                return;
            }

            if (!isSpectating(player)) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "SPECTATE.prevent.pvp");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player) || !isSpectating((Player) event.getEntity())) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
