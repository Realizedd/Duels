package me.realized.duels.spectate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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
import me.realized.duels.hook.hooks.EssentialsHook;
import me.realized.duels.hook.hooks.MyPetHook;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.teleport.Teleport;
import me.realized.duels.util.BlockUtil;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.compat.CompatUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpectateManagerImpl implements Loadable, SpectateManager {

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final ArenaManagerImpl arenaManager;
    private final PlayerInfoManager playerManager;

    // Map UUID to a spectator
    private final Map<UUID, SpectatorImpl> spectators = new HashMap<>();
    // Map arena to a collection of spectators
    private final Multimap<Arena, SpectatorImpl> arenas = HashMultimap.create();

    private Teleport teleport;
    private MyPetHook myPet;
    private EssentialsHook essentials;

    public SpectateManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();

        Bukkit.getPluginManager().registerEvents(new SpectateListener(), plugin);
    }

    @Override
    public void handleLoad() {
        // Late-init since SpectateManager is loaded before below variables are loaded
        this.teleport = plugin.getTeleport();
        this.myPet = plugin.getHookManager().getHook(MyPetHook.class);
        this.essentials = plugin.getHookManager().getHook(EssentialsHook.class);
    }

    @Override
    public void handleUnload() {
        spectators.clear();
    }

    @Nullable
    @Override
    public SpectatorImpl get(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return spectators.get(player.getUniqueId());
    }

    @Override
    public boolean isSpectating(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player) != null;
    }

    @NotNull
    @Override
    public Result startSpectating(@NotNull final Player player, @NotNull final Player target) {
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
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return Result.EVENT_CANCELLED;
        }

        final MatchImpl match = arena.getMatch();

        // Hide from players in match
        if (match != null && !(essentials != null && essentials.isVanished(player))) {
            match.getAllPlayers()
                .stream()
                .filter(arenaPlayer -> arenaPlayer.isOnline() && arenaPlayer.canSee(player))
                .forEach(arenaPlayer -> {
                    if (CompatUtil.hasHidePlayer()) {
                        arenaPlayer.hidePlayer(plugin, player);
                    } else {
                        arenaPlayer.hidePlayer(player);
                    }
                });
        }

        // Remove pet before teleport
        if (myPet != null) {
            myPet.removePet(player);
        }

        // Cache current player state to return to after spectating is over
        playerManager.create(player);
        PlayerUtil.reset(player);

        // Teleport before putting in map to prevent teleport being cancelled
        teleport.tryTeleport(player, target.getLocation().clone().add(0, 2, 0));
        spectators.put(player.getUniqueId(), spectator);
        arenas.put(arena, spectator);

        if (!config.isSpecUseSpectatorGamemode()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            player.setGameMode(GameMode.SPECTATOR);
        }

        if (CompatUtil.hasSetCollidable()) {
            player.setCollidable(false);
        } else {
            player.spigot().setCollidesWithEntities(false);
        }

        if (config.isSpecAddInvisibilityEffect()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
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
     * @param player Player to put out of spectator mode
     * @param spectator {@link SpectatorImpl} instance associated to this player
     */
    public void stopSpectating(final Player player, final SpectatorImpl spectator) {
        spectators.remove(player.getUniqueId());
        arenas.remove(spectator.getArena(), spectator);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlying(false);
        player.setAllowFlight(false);
        PlayerUtil.reset(player);

        if (CompatUtil.hasSetCollidable()) {
            player.setCollidable(true);
        } else {
            player.spigot().setCollidesWithEntities(true);
        }

        final PlayerInfo info = playerManager.remove(player);

        if (info != null) {
            teleport.tryTeleport(player, info.getLocation());
            info.restore(player);
        } else {
            teleport.tryTeleport(player, playerManager.getLobby());
        }

        final MatchImpl match = spectator.getArena().getMatch();

        // Show to players in match
        if (match != null && !(essentials != null && essentials.isVanished(player))) {
            match.getAllPlayers()
                .stream()
                .filter(Player::isOnline)
                .forEach(arenaPlayer -> {
                    if (CompatUtil.hasHidePlayer()) {
                        arenaPlayer.showPlayer(plugin, player);
                    } else {
                        arenaPlayer.showPlayer(player);
                    }
                });
        }

        final SpectateEndEvent event = new SpectateEndEvent(player, spectator);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Puts player out of spectator mode.
     *
     * @see #stopSpectating(Player, SpectatorImpl)
     */
    @Override
    public void stopSpectating(@NotNull final Player player) {
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
            final Player player = Bukkit.getPlayer(spectator.getUuid());

            if (player == null) {
                return;
            }

            stopSpectating(player, spectator);
            lang.sendMessage(player, "SPECTATE.match-end");
        });
    }

    @NotNull
    @Override
    public List<Spectator> getSpectators(@NotNull final Arena arena) {
        Objects.requireNonNull(arena, "arena");
        return Collections.unmodifiableList(Lists.newArrayList(getSpectatorsImpl(arena)));
    }

    public Collection<SpectatorImpl> getSpectatorsImpl(final Arena arena) {
        return arenas.asMap().getOrDefault(arena, Collections.emptyList());
    }

    public Collection<Player> getAllSpectators() {
        return spectators.values()
            .stream()
            .map(spectator -> Bukkit.getPlayer(spectator.getUuid()))
            .collect(Collectors.toList());
    }

    private class SpectateListener implements Listener {

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
        public void on(final PlayerInteractEvent event) {
            if (!isSpectating(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerPickupItemEvent event) {
            if (!isSpectating(event.getPlayer())) {
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
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player) || !isSpectating((Player) event.getEntity())) {
                return;
            }

            event.setCancelled(true);
        }

        // Prevents spectators (in adventure mode) blocking players from placing blocks.
        @EventHandler
        public void on(final BlockCanBuildEvent event) {
            if (!CompatUtil.hasGetPlayer() || config.isSpecUseSpectatorGamemode()) {
                return;
            }

            final Player player = event.getPlayer();

            if (player == null || BlockUtil.near(player, event.getBlock(), 0, 2)) {
                return;
            }

            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null) {
                return;
            }

            for (final SpectatorImpl spectator : getSpectatorsImpl(arena)) {
                final Player specPlayer = spectator.getPlayer();

                if (specPlayer == null) {
                    continue;
                }

                if (BlockUtil.near(specPlayer, event.getBlock(), 1, 2)) {
                    event.setBuildable(true);
                    break;
                }
            }
        }
    }
}
