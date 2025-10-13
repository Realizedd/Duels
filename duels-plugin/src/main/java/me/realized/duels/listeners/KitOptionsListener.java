package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.arena.MatchImpl;
import me.realized.duels.config.Config;
import me.realized.duels.kit.KitImpl.Characteristic;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.metadata.MetadataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Applies kit characteristics (options) to duels.
 */
public class KitOptionsListener implements Listener {

    private static final String METADATA_KEY = "Duels-MaxNoDamageTicks";

    private final DuelsPlugin plugin;
    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public KitOptionsListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(CompatUtil.isPre1_14() ? new ComboPre1_14Listener() : new ComboPost1_14Listener(), plugin);
    }

    private boolean isEnabled(final ArenaImpl arena, final Characteristic characteristic) {
        final MatchImpl match = arena.getMatch();
        return match != null && match.getKit() != null && match.getKit().hasCharacteristic(characteristic);
    }

    @EventHandler
    public void on(final EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || !isEnabled(arena, Characteristic.SUMO) && !isEnabled(arena, Characteristic.BOXING)) {
            return;
        }

        event.setDamage(0);
    }

    @EventHandler
    public void on(final FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || !isEnabled(arena, Characteristic.HUNGER)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onInteractLiquid(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.hasBlock()) {
            return;
        }

        ArenaImpl arena = arenaManager.get(event.getPlayer());

        if (arena == null || !isEnabled(arena, Characteristic.PLACE)) {
            return;
        }

        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        String itemType = item.getType().name().toLowerCase();

        if (!itemType.contains("water") && !itemType.contains("lava")) {
            return;
        }

        arena.getMatch().liquids.add(event.getClickedBlock());
    }

    @EventHandler
    public void on(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null) {
            return;
        }

        if(isEnabled(arena, Characteristic.PLACE)) {
            Material blockType = event.getBlockReplacedState().getType();
            if(blockType == Material.AIR || blockType == Material.WATER || blockType == Material.LAVA) {
                arena.getMatch().placedBlocks.add(event.getBlock());
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || arena.getMatch() == null) {
            return;
        }

        if(arena.getMatch().placedBlocks.contains(event.getBlock())) {
            return;
        }

        if(isEnabled(arena, Characteristic.BREAK)) {
            if(arena.getMatch().brokenBlocks.containsKey(event.getBlock().getLocation())) {
                return;
            }

            arena.getMatch().brokenBlocks.put(event.getBlock().getLocation(), event.getBlock().getBlockData().clone());
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void on(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);

        if (player.isDead() || arena == null || !isEnabled(arena, Characteristic.SUMO) || arena.isEndGame()) {
            return;
        }

        final Location to = event.getTo(), from = event.getFrom();

        if ((from.getBlockX() !=
                to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ())
             && arena.getCountdown() != null) {
            from.setPitch(player.getLocation().getPitch());
            from.setYaw(player.getLocation().getYaw());
            event.setTo(from);
            return;
        }

        final Block block = event.getFrom().getBlock();

        if (!(block.getType().name().contains("WATER") || block.getType().name().contains("LAVA"))) {
            return;
        }

        player.setHealth(0);
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        if (!event.hasItem() || !event.getAction().name().contains("RIGHT")) {
            return;
        }

        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || !isEnabled(arena, Characteristic.SOUP)) {
            return;
        }

        final ItemStack item = event.getItem();

        if (item == null || item.getType() != Items.MUSHROOM_SOUP) {
            return;
        }

        event.setUseItemInHand(Result.DENY);

        if (config.isSoupCancelIfAlreadyFull() && player.getHealth() == PlayerUtil.getMaxHealth(player)) {
            return;
        }

        final ItemStack bowl = config.isSoupRemoveEmptyBowl() ? null : new ItemStack(Material.BOWL);

        if (CompatUtil.isPre1_10()) {
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), bowl);
        } else {
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(bowl);
            } else {
                player.getInventory().setItemInMainHand(bowl);
            }
        }

        final double regen = config.getSoupHeartsToRegen() * 2.0;
        final double oldHealth = player.getHealth();
        final double maxHealth = PlayerUtil.getMaxHealth(player);
        player.setHealth(Math.min(oldHealth + regen, maxHealth));
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getRegainReason() == RegainReason.SATIATED || event.getRegainReason() == RegainReason.REGEN)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final ArenaImpl arena = arenaManager.get(player);

        if (arena == null || !isEnabled(arena, Characteristic.UHC)) {
            return;
        }

        event.setCancelled(true);
    }

    private class ComboPre1_14Listener implements Listener {

        @EventHandler
        public void on(final MatchStartEvent event) {
            final ArenaImpl arena = arenaManager.get(event.getMatch().getArena().getName());

            if (arena == null || !isEnabled(arena, Characteristic.COMBO)) {
                return;
            }

            for (final Player player : event.getPlayers()) {
                MetadataUtil.put(plugin, player, METADATA_KEY, player.getMaximumNoDamageTicks());
                player.setMaximumNoDamageTicks(0);
            }
        }

        @EventHandler
        public void on(final MatchEndEvent event) {
            final ArenaImpl arena = arenaManager.get(event.getMatch().getArena().getName());

            if (arena == null || !isEnabled(arena, Characteristic.COMBO)) {
                return;
            }

            final MatchImpl match = arena.getMatch();

            if (match == null) {
                return;
            }

            match.getAllPlayers().forEach(player -> {
                final Object value = MetadataUtil.removeAndGet(plugin, player, METADATA_KEY);

                if (value == null) {
                    return;
                }

                player.setMaximumNoDamageTicks((Integer) value);
            });
        }
    }

    private class ComboPost1_14Listener implements Listener {

        @EventHandler
        public void on(final EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Player)) {
                return;
            }

            final Player player = (Player) event.getEntity();
            final ArenaImpl arena = arenaManager.get(player);

            if (arena == null || !isEnabled(arena, Characteristic.COMBO)) {
                return;
            }

            plugin.doSyncAfter(() -> player.setNoDamageTicks(0), 1);
        }
    }
}
