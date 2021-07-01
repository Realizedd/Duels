package me.realized.duels.gui.betting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.duel.DuelManager;
import me.realized.duels.gui.betting.buttons.CancelButton;
import me.realized.duels.gui.betting.buttons.DetailsButton;
import me.realized.duels.gui.betting.buttons.HeadButton;
import me.realized.duels.gui.betting.buttons.StateButton;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.compat.CompatUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.AbstractGui;
import me.realized.duels.util.gui.Button;
import me.realized.duels.util.gui.GuiListener;
import me.realized.duels.util.inventory.InventoryBuilder;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BettingGui extends AbstractGui<DuelsPlugin> {

    private final Section[] sections = {
            new Section(9, 13, 4),
            new Section(14, 18, 4)
    };
    private final GuiListener<DuelsPlugin> guiListener;
    private final DuelManager duelManager;
    private final Settings settings;
    private final Inventory inventory;
    private final UUID first, second;
    private boolean firstReady, secondReady, waitDone, cancelWait;

    public BettingGui(final DuelsPlugin plugin, final Settings settings, final Player first, final Player second) {
        super(plugin);
        this.guiListener = plugin.getGuiListener();
        this.duelManager = plugin.getDuelManager();
        this.settings = settings;
        this.inventory = InventoryBuilder.of(plugin.getLang().getMessage("GUI.item-betting.title"), 54).build();
        this.first = first.getUniqueId();
        this.second = second.getUniqueId();
        set(inventory, 13, 14, 5, new CancelButton(plugin));
        Slots.run(0, 3, slot -> inventory.setItem(slot, Items.ORANGE_PANE.clone()));
        Slots.run(45, 48, slot -> inventory.setItem(slot, Items.ORANGE_PANE.clone()));
        Slots.run(6, 9, slot -> inventory.setItem(slot, Items.BLUE_PANE.clone()));
        Slots.run(51, 54, slot -> inventory.setItem(slot, Items.BLUE_PANE.clone()));
        set(inventory, 3, new StateButton(plugin, this, first));
        set(inventory, 4, new DetailsButton(plugin, settings));
        set(inventory, 5, new StateButton(plugin, this, second));
        set(inventory, 48, new HeadButton(plugin, first));
        set(inventory, 50, new HeadButton(plugin, second));
    }

    private boolean isFirst(final Player player) {
        return player.getUniqueId().equals(first);
    }

    private Section getSection(final Player player) {
        return isFirst(player) ? sections[0] : sections[1];
    }

    public boolean isReady(final Player player) {
        return isFirst(player) ? firstReady : secondReady;
    }

    public void setReady(final Player player) {
        if (isFirst(player)) {
            firstReady = true;
        } else {
            secondReady = true;
        }

        if (firstReady && secondReady) {
            new WaitTask().runTaskTimer(plugin, 10L, 20L);
        }
    }

    public void update(final Player player, final Button<DuelsPlugin> button) {
        update(player, inventory, button);
    }

    @Override
    public void open(final Player... players) {
        for (final Player player : players) {
            update(player);
            player.openInventory(inventory);
        }
    }

    @Override
    public boolean isPart(final Inventory inventory) {
        return inventory.equals(this.inventory);
    }

    @Override
    public void on(final Player player, final Inventory top, final InventoryClickEvent event) {
        final Inventory clicked = event.getClickedInventory();

        if (clicked == null) {
            return;
        }

        final Button<DuelsPlugin> button = get(inventory, event.getSlot());

        if (firstReady && secondReady && !(button instanceof CancelButton)) {
            event.setCancelled(true);
            return;
        }

        final int slot = event.getSlot();

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }

        if (!clicked.equals(top)) {
            return;
        }

        final Section section = getSection(player);

        if (section == null) {
            return;
        }

        if (!isReady(player) && section.isPart(slot)) {
            return;
        }

        event.setCancelled(true);

        if (button == null) {
            return;
        }

        button.onClick(player);
    }

    @Override
    public void on(final Player player, final Set<Integer> rawSlots, final InventoryDragEvent event) {
        final Section section = getSection(player);

        if (section == null) {
            return;
        }

        boolean in = false;
        boolean out = false;
        boolean outSec = false;

        for (final int slot : rawSlots) {
            if (slot > 53) {
                out = true;
            } else {
                if (!section.isPart(slot)) {
                    outSec = true;
                }

                in = true;
            }
        }

        if (in && (isReady(player) || out || outSec)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void on(final Player player, final Inventory inventory, final InventoryCloseEvent event) {
        if (waitDone) {
            return;
        }

        cancelWait = true;
        getSection(player).collect().forEach(item -> player.getInventory().addItem(item));
        guiListener.removeGui(player, this);

        final Player other = Bukkit.getPlayer(first.equals(player.getUniqueId()) ? second : first);

        if (other != null) {
            plugin.doSync(() -> {
                if (inventory.getViewers().contains(other)) {
                    other.closeInventory();
                }
            });
        }
    }

    @Override
    public void clear() {
        final Player first = Bukkit.getPlayer(BettingGui.this.first);
        final Player second = Bukkit.getPlayer(BettingGui.this.second);

        if (first != null && second != null) {
            getSection(first).collect().forEach(item -> first.getInventory().addItem(item));
            getSection(second).collect().forEach(item -> second.getInventory().addItem(item));
        }

        super.clear();
    }

    private class Section {

        private final int start, end, height;

        Section(final int start, final int end, final int height) {
            this.start = start;
            this.end = end;
            this.height = height;
        }

        private boolean isPart(final int slot) {
            for (int y = 0; y < height; y++) {
                for (int x = start; x < end; x++) {
                    if (x + y * 9 == slot) {
                        return true;
                    }
                }
            }

            return false;
        }

        private List<ItemStack> collect() {
            final List<ItemStack> result = new ArrayList<>();
            Slots.run(start, end, height, slot -> {
                final ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != Material.AIR) {
                    result.add(item);
                    inventory.setItem(slot, null);
                }
            });
            return result;
        }
    }

    private class WaitTask extends BukkitRunnable {

        private static final int SLOT_START = 13;

        private int counter;

        @Override
        public void run() {
            if (cancelWait) {
                cancel();
                return;
            }

            if (counter < 5) {
                final int slot = SLOT_START + 9 * counter;
                ItemStack item = inventory.getItem(slot);

                if (CompatUtil.isPre1_13()) {
                    item.setDurability((short) 5);
                } else {
                    final ItemStack greenPane = Items.GREEN_PANE.clone();
                    greenPane.setItemMeta(item.getItemMeta());
                    item = greenPane;
                }

                inventory.setItem(slot, item);
                counter++;
                return;
            }

            cancel();
            waitDone = true;

            final Player first = Bukkit.getPlayer(BettingGui.this.first);
            final Player second = Bukkit.getPlayer(BettingGui.this.second);

            if (first == null || second == null) {
                return;
            }

            first.closeInventory();
            second.closeInventory();

            final Map<UUID, List<ItemStack>> items = new HashMap<>();
            items.put(first.getUniqueId(), getSection(first).collect());
            items.put(second.getUniqueId(), getSection(second).collect());

            guiListener.removeGui(first, BettingGui.this);
            guiListener.removeGui(second, BettingGui.this);
            duelManager.startMatch(first, second, settings, items, null);
        }
    }
}
