package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.api.event.match.MatchStartEvent;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.arena.MatchImpl;
import me.realized.duels.config.Config;
import me.realized.duels.kit.KitImpl.Characteristic;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.metadata.MetadataUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Applies kit characteristics (options) to duels.
 */
public class KitOptionsListener implements Listener {

    public static final String METADATA_KEY = "Duels-MaxNoDamageTicks";

    private final DuelsPlugin plugin;
    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public KitOptionsListener(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        // Do not register the listener if own inventory is enabled
        if (config.isUseOwnInventoryEnabled()) {
            return;
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getPluginManager().registerEvents(CompatUtil.isPre1_14() ? new ComboPre1_14Listener() : new ComboPost1_14Listener(), plugin);
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

        if (arena == null || !isEnabled(arena, Characteristic.SUMO)) {
            return;
        }

        event.setDamage(0);
    }

    @EventHandler
    public void on(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final ArenaImpl arena = arenaManager.get(player);

        if (player.isDead() || arena == null || !isEnabled(arena, Characteristic.SUMO)) {
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

        if (config.isSoupCancelIfAlreadyFull() && player.getHealth() == player.getMaxHealth()) {
            return;
        }

        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), config.isSoupRemoveEmptyBowl() ? null : new ItemStack(Material.BOWL));

        final double regen = config.getSoupHeartsToRegen() * 2.0;
        final double oldHealth = player.getHealth();
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
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
