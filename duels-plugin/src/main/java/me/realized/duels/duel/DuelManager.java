package me.realized.duels.duel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchEndEvent.Reason;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.arena.MatchImpl;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManagerImpl;
import me.realized.duels.hook.hooks.CombatLogXHook;
import me.realized.duels.hook.hooks.CombatTagPlusHook;
import me.realized.duels.hook.hooks.EssentialsHook;
import me.realized.duels.hook.hooks.McMMOHook;
import me.realized.duels.hook.hooks.MyPetHook;
import me.realized.duels.hook.hooks.PvPManagerHook;
import me.realized.duels.hook.hooks.VaultHook;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.inventories.InventoryManager;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.player.PlayerInfoManager;
import me.realized.duels.queue.Queue;
import me.realized.duels.queue.QueueManager;
import me.realized.duels.setting.Settings;
import me.realized.duels.teleport.Teleport;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.function.Pair;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class DuelManager implements Loadable {

    private static final Calendar GREGORIAN_CALENDAR = new GregorianCalendar();

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final UserManagerImpl userDataManager;
    private final ArenaManagerImpl arenaManager;
    private final PlayerInfoManager playerManager;
    private final InventoryManager inventoryManager;
    private final DuelManagerHandler duelManagerHandler = new DuelManagerHandler(this);

    private QueueManager queueManager;
    private Teleport teleport;
    private CombatTagPlusHook combatTagPlus;
    private PvPManagerHook pvpManager;
    private CombatLogXHook combatLogX;
    private VaultHook vault;
    private EssentialsHook essentials;
    private McMMOHook mcMMO;
    private WorldGuardHook worldGuard;
    private MyPetHook myPet;

    private int durationCheckTask;

    public DuelManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userDataManager = plugin.getUserManager();
        this.arenaManager = plugin.getArenaManager();
        this.playerManager = plugin.getPlayerManager();
        this.inventoryManager = plugin.getInventoryManager();

        plugin.doSyncAfter(() -> Bukkit.getPluginManager().registerEvents(new DuelListener(), plugin), 1L);
    }

    @Override
    public void handleLoad() {

        duelManagerHandler.handleLoad();
    }

    @Override
    public void handleUnload() {

        /*
        3 Cases:
        1. size = 2: Match outcome is yet to be decided (INGAME phase)
        2. size = 1: Match ended with a winner and is in ENDGAME phase
        3. size = 0: Match ended in a tie (or winner killed themselves during ENDGAME phase) and is in ENDGAME phase
        */
        duelManagerHandler.handleUnload();
    }

    /**
     * Resets the player's inventory and balance in the case of a tie game.
     *
     * @param player Player to reset state
     * @param arena Arena the match is taking place
     * @param match Match the player is in
     * @param alive Whether the player was alive in the match when the method was called.
     */
    private void handleTie(final Player player, final ArenaImpl arena, final MatchImpl match, boolean alive) {

        // Reset player balance if there was a bet placed.

        duelManagerHandler.handleTie(player, arena, match, alive);
    }

    /**
     * Rewards the duel winner with money and items bet on the match.
     *
     * @param player Player determined to be the winner
     * @param opponent Player that opposed the winner
     * @param arena Arena the match is taking place
     * @param match Match the player is in
     */
    private void handleWin(final Player player, final Player opponent, final ArenaImpl arena, final MatchImpl match) {

        duelManagerHandler.handleWin(player, opponent, arena, match);
    }

    public void startMatch(final Player first, final Player second, final Settings settings, final Map<UUID, List<ItemStack>> items, final Queue source) {
        final KitImpl kit = settings.getKit();

        if (!settings.isOwnInventory() && kit == null) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.mode-unselected");
            refundItems(items, first, second);
            return;
        }

        if (first.isDead() || second.isDead()) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.player-is-dead");
            refundItems(items, first, second);
            return;
        }

        if (isBlacklistedWorld(first) || isBlacklistedWorld(second)) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.in-blacklisted-world");
            refundItems(items, first, second);
            return;
        }

        if (isTagged(first) || isTagged(second)) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.is-tagged");
            refundItems(items, first, second);
            return;
        }

        if (config.isCancelIfMoved() && (notInLoc(first, settings.getBaseLoc(first)) || notInLoc(second, settings.getBaseLoc(second)))) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.player-moved");
            refundItems(items, first, second);
            return;
        }

        if (config.isDuelzoneEnabled() && worldGuard != null && (notInDz(first, settings.getDuelzone(first)) || notInDz(second, settings.getDuelzone(second)))) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.not-in-duelzone");
            refundItems(items, first, second);
            return;
        }

        if (config.isPreventCreativeMode() && (first.getGameMode() == GameMode.CREATIVE || second.getGameMode() == GameMode.CREATIVE)) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.in-creative-mode");
            refundItems(items, first, second);
            return;
        }

        final ArenaImpl arena = settings.getArena() != null ? settings.getArena() : arenaManager.randomArena(kit);

        if (arena == null || !arena.isAvailable()) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure." + (settings.getArena() != null ? "arena-in-use" : "no-arena-available"));
            refundItems(items, first, second);
            return;
        }

        if (kit != null && !arenaManager.isSelectable(kit, arena)) {
            lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.arena-not-applicable", "kit", kit.getName(), "arena", arena.getName());
            refundItems(items, first, second);
            return;
        }

        final int bet = settings.getBet();

        if (bet > 0 && vault != null && vault.getEconomy() != null) {
            if (!vault.has(bet, first, second)) {
                lang.sendMessage(Arrays.asList(first, second), "DUEL.start-failure.not-enough-money", "bet_amount", bet);
                refundItems(items, first, second);
                return;
            }

            vault.remove(bet, first, second);
        }

        final MatchImpl match = arena.startMatch(kit, items, settings.getBet(), source);
        addPlayers(source, arena, kit, arena.getPositions(), first, second);

        if (config.isCdEnabled()) {
            final Map<UUID, Pair<String, Integer>> info = new HashMap<>();
            info.put(first.getUniqueId(), new Pair<>(second.getName(), getRating(kit, userDataManager.get(second))));
            info.put(second.getUniqueId(), new Pair<>(first.getName(), getRating(kit, userDataManager.get(first))));
            arena.startCountdown(kit != null ? kit.getName() : lang.getMessage("GENERAL.none"), info);
        }

        final MatchStartEvent event = new MatchStartEvent(match, first, second);
        Bukkit.getPluginManager().callEvent(event);
    }

    private void refundItems(final Map<UUID, List<ItemStack>> items, final Player... players) {
        if (items != null) {
            Arrays.stream(players).forEach(player -> InventoryUtil.addOrDrop(player, items.getOrDefault(player.getUniqueId(), Collections.emptyList())));
        }
    }

    private boolean isBlacklistedWorld(final Player player) {
        return config.getBlacklistedWorlds().contains(player.getWorld().getName());
    }

    private boolean isTagged(final Player player) {
        return (combatTagPlus != null && combatTagPlus.isTagged(player))
            || (pvpManager != null && pvpManager.isTagged(player))
            || (combatLogX != null && combatLogX.isTagged(player));
    }

    private boolean notInLoc(final Player player, final Location location) {
        if (location == null) {
            return false;
        }

        final Location source = player.getLocation();
        return !source.getWorld().equals(location.getWorld())
            || source.getBlockX() != location.getBlockX()
            || source.getBlockY() != location.getBlockY()
            || source.getBlockZ() != location.getBlockZ();
    }

    private boolean notInDz(final Player player, final String duelzone) {
        return duelzone != null && !duelzone.equals(worldGuard.findDuelZone(player));
    }

    private int getRating(final KitImpl kit, final UserData user) {
        return user != null ? user.getRating(kit) : config.getDefaultRating();
    }

    private void addPlayers(final Queue source, final ArenaImpl arena, final KitImpl kit, final Map<Integer, Location> locations, final Player... players) {
        int position = 0;

        for (final Player player : players) {
            if (source == null) {
                queueManager.remove(player);
            }

            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }

            player.closeInventory();
            playerManager.create(player);
            teleport.tryTeleport(player, locations.get(++position));

            if (kit != null) {
                PlayerUtil.reset(player);
                kit.equip(player);
            }

            if (config.isStartCommandsEnabled() && !(source == null && config.isStartCommandsQueueOnly())) {
                try {
                    for (final String command : config.getStartCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                    }
                } catch (Exception ex) {
                    Log.warn(this, "Error while running match start commands: " + ex.getMessage());
                }
            }

            if (myPet != null) {
                myPet.removePet(player);
            }

            if (essentials != null) {
                essentials.tryUnvanish(player);
            }

            if (mcMMO != null) {
                mcMMO.disableSkills(player);
            }

            arena.add(player);
        }
    }

    private void handleInventories(final MatchImpl match) {

        duelManagerHandler.handleInventories(match);
    }

    private void handleStats(final MatchImpl match, final UserData winner, final UserData loser, final MatchData matchData) {
        duelManagerHandler.handleStats(match, winner, loser, matchData);
    }

    public PvPManagerHook getPvpManager() {
        return pvpManager;
    }

    public Lang getLang() {
        return lang;
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public CombatTagPlusHook getCombatTagPlus() {
        return combatTagPlus;
    }

    public CombatLogXHook getCombatLogX() {
        return combatLogX;
    }

    public Config getConfig() {
        return config;
    }

    public int getDurationCheckTask() {
        return durationCheckTask;
    }

    public Teleport getTeleport() {
        return teleport;
    }

    public ArenaManagerImpl getArenaManager() {
        return arenaManager;
    }

    public EssentialsHook getEssentials() {
        return essentials;
    }

    public WorldGuardHook getWorldGuard() {
        return worldGuard;
    }

    public MyPetHook getMyPet() {
        return myPet;
    }

    public PlayerInfoManager getPlayerManager() {
        return playerManager;
    }

    public VaultHook getVault() {
        return vault;
    }

    public McMMOHook getMcMMO() {
        return mcMMO;
    }

    public DuelsPlugin getPlugin() {
        return plugin;
    }

    public void setPvpManager(PvPManagerHook pvpManager) {
        this.pvpManager = pvpManager;
    }

    public void setMyPet(MyPetHook myPet) {
        this.myPet = myPet;
    }

    public void setQueueManager(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public void setCombatTagPlus(CombatTagPlusHook combatTagPlus) {
        this.combatTagPlus = combatTagPlus;
    }

    public void setCombatLogX(CombatLogXHook combatLogX) {
        this.combatLogX = combatLogX;
    }

    public void setDurationCheckTask(int durationCheckTask) {
        this.durationCheckTask = durationCheckTask;
    }

    public void setVault(VaultHook vault) {
        this.vault = vault;
    }

    public void setMcMMO(McMMOHook mcMMO) {
        this.mcMMO = mcMMO;
    }

    public void setTeleport(Teleport teleport) {
        this.teleport = teleport;
    }

    public void setEssentials(EssentialsHook essentials) {
        this.essentials = essentials;
    }

    public void setWorldGuard(WorldGuardHook worldGuard) {
        this.worldGuard = worldGuard;
    }

    private class DuelListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onLowest(final PlayerDeathEvent event) {
            if (!arenaManager.isInMatch(event.getEntity())) {
                return;
            }

            event.getDrops().clear();
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void on(final PlayerDeathEvent event) {
            final Player player = event.getEntity();
            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null) {
                return;
            }

            if (mcMMO != null) {
                mcMMO.enableSkills(player);
            }

            event.setKeepLevel(true);
            event.setDroppedExp(0);
            event.setKeepInventory(false);
            inventoryManager.create(player, true);

            final MatchImpl match = arena.getMatch();

            if (match == null) {
                return;
            }

            arena.remove(player);

            // Call end task only on the first death
            if (arena.size() <= 0) {
                return;
            }

            plugin.doSyncAfter(() -> {
                if (arena.size() == 0) {
                    match.getAllPlayers().forEach(matchPlayer -> {
                        duelManagerHandler.handleTie(matchPlayer, arena, match, false);
                        lang.sendMessage(matchPlayer, "DUEL.on-end.tie");
                    });
                    plugin.doSyncAfter(() -> duelManagerHandler.handleInventories(match), 1L);
                    arena.endMatch(null, null, Reason.TIE);
                    return;
                }

                final Player winner = arena.first();
                inventoryManager.create(winner, false);

                if (config.isSpawnFirework()) {
                    final Firework firework = (Firework) winner.getWorld().spawnEntity(winner.getEyeLocation(), EntityType.FIREWORK);
                    final FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder().withColor(Color.RED).with(FireworkEffect.Type.BALL_LARGE).withTrail().build());
                    firework.setFireworkMeta(meta);
                }

                final double health = Math.ceil(winner.getHealth()) * 0.5;
                final String kitName = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
                final long duration = System.currentTimeMillis() - match.getStart();
                final long time = GREGORIAN_CALENDAR.getTimeInMillis();
                final MatchData matchData = new MatchData(winner.getName(), player.getName(), kitName, time, duration, health);
                duelManagerHandler.handleStats(match, userDataManager.get(winner), userDataManager.get(player), matchData);
                plugin.doSyncAfter(() -> duelManagerHandler.handleInventories(match), 1L);
                plugin.doSyncAfter(() -> {
                    duelManagerHandler.handleWin(winner, player, arena, match);

                    if (config.isEndCommandsEnabled() && !(!match.isFromQueue() && config.isEndCommandsQueueOnly())) {
                        try {
                            for (final String command : config.getEndCommands()) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                                    .replace("%winner%", winner.getName()).replace("%loser%", player.getName())
                                    .replace("%kit%", kitName).replace("%arena%", arena.getName())
                                    .replace("%bet_amount%", String.valueOf(match.getBet()))
                                );
                            }
                        } catch (Exception ex) {
                            Log.warn(DuelManager.this, "Error while running match end commands: " + ex.getMessage());
                        }
                    }

                    arena.endMatch(winner.getUniqueId(), player.getUniqueId(), Reason.OPPONENT_DEFEAT);
                }, config.getTeleportDelay() * 20L);
            }, 1L);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player)) {
                return;
            }

            final Player player = (Player) event.getEntity();
            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null || !arena.isEndGame()) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler
        public void on(final PlayerQuitEvent event) {
            final Player player = event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            player.setHealth(0);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerDropItemEvent event) {
            if (!config.isPreventItemDrop() || !arenaManager.isInMatch(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(event.getPlayer(), "DUEL.prevent.item-drop");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerPickupItemEvent event) {
            // Fix players not being able to use the Loyalty enchantment in a duel if item pickup is disabled in config.
            if (!CompatUtil.isPre1_13() && event.getItem().getItemStack().getType() == Material.TRIDENT) {
                return;
            }

            if (!config.isPreventItemPickup() || !arenaManager.isInMatch(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerCommandPreprocessEvent event) {
            final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

            if (!arenaManager.isInMatch(event.getPlayer())
                || (config.isBlockAllCommands() ? config.getWhitelistedCommands().contains(command) : !config.getBlacklistedCommands().contains(command))) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(event.getPlayer(), "DUEL.prevent.command", "command", event.getMessage());
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerTeleportEvent event) {
            final Player player = event.getPlayer();
            final Location to = event.getTo();

            if (!config.isLimitTeleportEnabled()
                || event.getCause() == TeleportCause.ENDER_PEARL
                || event.getCause() == TeleportCause.SPECTATE
                || !arenaManager.isInMatch(player)) {
                return;
            }

            final Location from = event.getFrom();

            if (from.getWorld().equals(to.getWorld()) && from.distance(to) <= config.getDistanceAllowed()) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "DUEL.prevent.teleportation");
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final InventoryOpenEvent event) {
            if (!config.isPreventInventoryOpen()) {
                return;
            }

            final Player player = (Player) event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            event.setCancelled(true);
            lang.sendMessage(player, "DUEL.prevent.inventory-open");
        }
    }
}