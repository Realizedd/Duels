package me.realized.duels.util.gui;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import me.realized.duels.util.Loadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiListener<P extends JavaPlugin> implements Loadable, Listener {

    private final Multimap<UUID, AbstractGui<P>> privateGuis = HashMultimap.create();
    private final List<AbstractGui<P>> publicGuis = new ArrayList<>();

    public GuiListener(final P plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {}

    @Override
    public void handleUnload() {
        privateGuis.values().forEach(AbstractGui::clear);
        privateGuis.clear();
        publicGuis.forEach(AbstractGui::clear);
        publicGuis.clear();
    }

    public void addGui(final AbstractGui<P> gui) {
        publicGuis.add(gui);
    }

    /**
     * @param removeSameType Prevents memory leaks in case of gui open failing for guis that remove themselves on inventory close.
     */
    public <T extends AbstractGui<P>> T addGui(final Player player, final T gui, final boolean removeSameType) {
        if (removeSameType) {
            final Collection<AbstractGui<P>> guis = privateGuis.asMap().get(player.getUniqueId());

            if (guis != null) {
                guis.removeIf(cached -> gui.getClass().isInstance(cached));
            }
        }

        privateGuis.put(player.getUniqueId(), gui);
        return gui;
    }

    public <T extends AbstractGui<P>> T addGui(final Player player, final T gui) {
        return addGui(player, gui, false);
    }

    public void removeGui(final AbstractGui<P> gui) {
        gui.clear();
        publicGuis.remove(gui);
    }

    public void removeGui(final Player player, final AbstractGui<P> gui) {
        gui.clear();

        final Collection<AbstractGui<P>> guis = privateGuis.asMap().get(player.getUniqueId());

        if (guis != null) {
            guis.remove(gui);
        }
    }

    private List<AbstractGui<P>> get(final Player player) {
        final List<AbstractGui<P>> guis = Lists.newArrayList(publicGuis);

        if (privateGuis.containsKey(player.getUniqueId())) {
            guis.addAll(privateGuis.get(player.getUniqueId()));
        }

        return guis;
    }

    @EventHandler
    public void on(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory top = player.getOpenInventory().getTopInventory();

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(top)) {
                gui.on(player, top, event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final InventoryDragEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inventory = event.getInventory();

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(inventory)) {
                gui.on(player, event.getRawSlots(), event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        final Inventory inventory = event.getInventory();

        for (final AbstractGui<P> gui : get(player)) {
            if (gui.isPart(inventory)) {
                gui.on(player, event.getInventory(), event);
                break;
            }
        }
    }

    @EventHandler
    public void on(final EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            final Inventory inventory = player.getOpenInventory().getTopInventory();
            for (final AbstractGui<P> gui : get(player)) {
                if (gui.isPart(inventory)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        privateGuis.removeAll(event.getPlayer().getUniqueId());
    }
}
