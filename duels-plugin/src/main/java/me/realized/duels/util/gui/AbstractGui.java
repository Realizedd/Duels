package me.realized.duels.util.gui;

import lombok.Getter;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractGui<P extends JavaPlugin> {

    protected final P plugin;

    @Getter
    private final long creation;

    private final Map<Inventory, Map<Integer, Button<P>>> buttons = new HashMap<>();

    public AbstractGui(final P plugin) {
        this.plugin = plugin;
        this.creation = System.currentTimeMillis();
    }

    public abstract void open(final Player... players);

    public abstract boolean isPart(final Inventory inventory);

    public abstract void on(final Player player, final Inventory top, final InventoryClickEvent event);

    public void on(final Player player, final Inventory inventory, final InventoryCloseEvent event) {
    }

    public void on(final Player player, final Set<Integer> rawSlots, final InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public Button<P> get(final Inventory inventory, final int slot) {
        final Map<Integer, Button<P>> buttons;
        return (buttons = this.buttons.get(inventory)) != null ? buttons.get(slot) : null;
    }

    public void set(final Inventory inventory, final int slot, final Button<P> button) {
        buttons.computeIfAbsent(inventory, result -> new HashMap<>()).put(slot, button);
        inventory.setItem(slot, button.getDisplayed());
    }

    public void set(final Inventory inventory, final int from, final int to, final int height, final Button<P> button) {
        Slots.run(from, to, height, slot -> set(inventory, slot, button));
    }

    public void set(final Inventory inventory, final int from, final int to, final Button<P> button) {
        Slots.run(from, to, slot -> set(inventory, slot, button));
    }

    public void remove(final Inventory inventory) {
        buttons.remove(inventory);
    }

    public Button<P> remove(final Inventory inventory, final int slot) {
        final Map<Integer, Button<P>> buttons;
        return (buttons = this.buttons.get(inventory)) != null ? buttons.remove(slot) : null;
    }

    public void update(final Player player, final Inventory inventory, final Button<P> button) {
        final Map<Integer, Button<P>> cached = buttons.get(inventory);

        if (cached == null) {
            return;
        }

        button.update(player);
        cached.entrySet().stream().filter(entry -> entry.getValue().equals(button)).findFirst().ifPresent(entry -> inventory.setItem(entry.getKey(), button.getDisplayed()));
    }

    public void update(final Player player) {
        buttons.forEach((inventory, data) -> data.forEach((slot, button) -> {
            button.update(player);
            inventory.setItem(slot, button.getDisplayed());
        }));
    }

    public void clear() {
        buttons.keySet().forEach(Inventory::clear);
    }
}
