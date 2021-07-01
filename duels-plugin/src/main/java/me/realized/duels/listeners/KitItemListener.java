package me.realized.duels.listeners;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.data.ItemData;
import me.realized.duels.util.Log;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Tags;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Prevents players from using kit items outside of a duel by checking
 * for an NBT tag stored in the item by Duels.
 */
public class KitItemListener implements Listener {

    // Warning sent to player attempting to use kit item
    private static final String WARNING = StringUtil.color("&4[Duels] Kit contents cannot be used when not in a duel.");

    // Warning printed on console
    private static final String WARNING_CONSOLE = "%s has attempted to use a kit item while not in duel, but was prevented by KitItemListener.";

    private final ArenaManagerImpl arenaManager;

    public KitItemListener(final DuelsPlugin plugin) {
        this.arenaManager = plugin.getArenaManager();

        // Only register listener if enabled in config.yml
        if (plugin.getConfiguration().isProtectKitItems()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    private boolean isExcluded(final Player player) {
        return player.isOp() || player.hasPermission(Permissions.ADMIN) || arenaManager.isInMatch(player);
    }

    private boolean isKitItem(final ItemStack item) {
        return item != null && item.getType() != Material.AIR && Tags.hasKey(item, ItemData.DUELS_ITEM_IDENTIFIER);
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();

        if (isExcluded(player)) {
            return;
        }

        final Inventory clicked = event.getClickedInventory();

        if (!(clicked instanceof PlayerInventory)) {
            return;
        }

        final ItemStack item = event.getCurrentItem();

        if (!isKitItem(item)) {
            return;
        }

        event.setCurrentItem(null);
        player.sendMessage(WARNING);
        Log.warn(String.format(WARNING_CONSOLE, player.getName()));
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (isExcluded(player)) {
            return;
        }

        final ItemStack item = event.getItem();

        if (!isKitItem(item)) {
            return;
        }

        event.setCancelled(true);
        player.getInventory().remove(item);
        player.sendMessage(WARNING);
        Log.warn(String.format(WARNING_CONSOLE, player.getName()));
    }

    @EventHandler
    public void on(final EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        final Player player = (Player) event.getEntity();
        if (isExcluded(player)) return;
        final Item item = event.getItem();
        if (!isKitItem(item.getItemStack())) return;
        event.setCancelled(true);
        item.remove();
        player.sendMessage(WARNING);
        Log.warn(String.format(WARNING_CONSOLE, player.getName()));
    }
}
