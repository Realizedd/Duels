package me.realized.duels.inventories;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.inventory.InventoryGui;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.gui.GuiListener;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager implements Loadable {

    private final DuelsPlugin plugin;
    private final GuiListener<DuelsPlugin> guiListener;
    private final Map<UUID, InventoryGui> inventories = new HashMap<>();

    private int expireTask;

    public InventoryManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.guiListener = plugin.getGuiListener();
    }

    @Override
    public void handleLoad() {
        this.expireTask = plugin.doSyncRepeat(() -> {
            final long now = System.currentTimeMillis();

            inventories.entrySet().removeIf(entry -> {
                if (now - entry.getValue().getCreation() >= 1000L * 60 * 5) {
                    guiListener.removeGui(entry.getValue());
                    return true;
                }

                return false;
            });
        }, 20L, 20L * 5).getTaskId();
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(expireTask);
        inventories.clear();
    }

    public InventoryGui get(final UUID uuid) {
        return inventories.get(uuid);
    }

    public void create(final Player player) {
        final InventoryGui gui = new InventoryGui(plugin, player);
        guiListener.addGui(gui);
        inventories.put(player.getUniqueId(), gui);
    }

    public void remove(final Player player) {
        final InventoryGui gui = inventories.remove(player.getUniqueId());

        if (gui != null) {
            guiListener.removeGui(gui);
        }
    }
}
