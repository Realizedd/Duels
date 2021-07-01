package me.realized.duels.util.gui;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.inventory.InventoryBuilder;
import me.realized.duels.util.inventory.Slots;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.function.Consumer;

public class MultiPageGui<P extends JavaPlugin> extends AbstractGui<P> {

    private final String title;
    private final int size, prevPageSlot, nextPageSlot;

    // Note: Referenced Collection!
    @Getter
    private final Collection<? extends Button<P>> buttons;

    private PageNode first;

    @Setter
    private ItemStack spaceFiller;
    @Setter
    private ItemStack prevButton;
    @Setter
    private ItemStack nextButton;
    @Setter
    private ItemStack emptyIndicator;

    public MultiPageGui(final P plugin, final String title, final int rows, final Collection<? extends Button<P>> buttons) {
        super(plugin);
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("title cannot be null or empty");
        }

        this.title = title;

        if (rows <= 0 || rows > 5) {
            throw new IllegalArgumentException("rows out of range, must be between 1 - 5");
        }

        this.size = rows * 9 + 9;
        this.prevPageSlot = size - 9;
        this.nextPageSlot = size - 1;

        if (buttons == null) {
            throw new IllegalArgumentException("buttons cannot be null");
        }

        this.buttons = buttons;
    }

    /**
     * Recalculates the pages for this {@link MultiPageGui}.
     */
    public void calculatePages() {
        // The max size an inventory can contain.
        final int maxSize = size - 9;
        // Total pages calculated based on the size of the buttons collection at this point of call.
        final int totalPages = buttons.size() / maxSize + (buttons.size() % maxSize > 0 ? 1 : 0);

        // If first time calculating, init first PageNode
        if (first == null) {
            first = createPage(1, totalPages);
        }

        // Case: The updated buttons collection is empty.
        if (totalPages == 0) {
            first.setEmpty();
            return;
        }

        int i = 0;
        int pageNum = 1;
        int slot = 0;
        PageNode last = null;

        for (final Button<P> button : buttons) {
            if (i % maxSize == 0) {
                final PageNode prev = last;

                if (last == null) {
                    last = first;
                } else {
                    if (last.next == null) {
                        last.next = createPage(pageNum, totalPages);
                    }

                    last = last.next;
                }

                //last.setTitle(title + " (" + pageNum + "/" + totalPages + ")");
                last.clear();

                if (prev != null) {
                    last.previous = prev;
                    last.inventory.setItem(prevPageSlot, prevButton);
                    prev.next = last;
                    prev.inventory.setItem(nextPageSlot, nextButton);
                }

                slot = 0;
                pageNum++;
            }

            set(last.inventory, slot, button);
            i++;
            slot++;
        }

        if (last != null) {
            last.resetNext();
        }
    }

    private PageNode createPage(final int page, final int total) {
        return new PageNode(InventoryBuilder
                .of(title + " (" + page + "/" + total + ")", size)
                .fillRange(prevPageSlot, nextPageSlot + 1, getSpaceFiller())
                .build());
    }

    private ItemStack getSpaceFiller() {
        return spaceFiller != null ? spaceFiller : Items.WHITE_PANE.clone();
    }

    @Override
    public void open(final Player... players) {
        for (final Player player : players) {
            player.openInventory(first.inventory);
        }
    }

    @Override
    public boolean isPart(final Inventory inventory) {
        return first.isPart(inventory);
    }

    @Override
    public void on(final Player player, final Inventory top, final InventoryClickEvent event) {
        final Inventory clicked = event.getClickedInventory();

        if (clicked == null) {
            return;
        }

        event.setCancelled(true);

        if (!clicked.equals(top)) {
            return;
        }

        final PageNode node = first.find(clicked);

        if (node == null) {
            return;
        }

        final int slot = event.getSlot();

        if (slot == nextPageSlot && node.next != null) {
            player.openInventory(node.next.inventory);
        } else if (slot == prevPageSlot && node.previous != null) {
            player.openInventory(node.previous.inventory);
        } else {
            final Button<P> button = get(clicked, slot);

            if (button == null) {
                return;
            }

            button.onClick(player);
        }
    }

    private class PageNode {

        private final Inventory inventory;
        private PageNode previous, next;

        PageNode(final Inventory inventory) {
            this.inventory = inventory;
        }

        void setEmpty() {
            //setTitle(title);

            final ItemStack item = inventory.getItem(4);

            if (item != null && item.isSimilar(emptyIndicator)) {
                return;
            }

            clear();
            resetBottom();
            inventory.setItem(4, emptyIndicator);
            remove(inventory);
            resetNext();
        }

        void resetNext() {
            if (next == null) {
                return;
            }

            inventory.setItem(nextPageSlot, getSpaceFiller());
            forEach(node -> {
                if (node.equals(this)) {
                    return;
                }

                remove(node.inventory);
                Lists.newArrayList(node.inventory.getViewers()).forEach(HumanEntity::closeInventory);
            });
            next = null;
        }

        //void setTitle(final String title) {
        //    inventory.getViewers().forEach();
        //    Inventories.setTitle(inventory, title);
        //}

        void clear() {
            remove(inventory);

            for (int slot = 0; slot < inventory.getSize() - 9; slot++) {
                inventory.setItem(slot, null);
            }
        }

        void resetBottom() {
            Slots.run(prevPageSlot, nextPageSlot + 1, slot -> inventory.setItem(slot, getSpaceFiller()));
        }

        void forEach(final Consumer<PageNode> consumer) {
            consumer.accept(this);

            if (next != null) {
                next.forEach(consumer);
            }
        }

        PageNode find(final Inventory inventory) {
            if (this.inventory.equals(inventory)) {
                return this;
            }

            if (next != null) {
                return next.find(inventory);
            }

            return null;
        }

        boolean isPart(final Inventory inventory) {
            return find(inventory) != null;
        }

//        boolean hasViewers() {
//            if (!inventory.getViewers().isEmpty()) {
//                return true;
//            }
//
//            return next != null && !next.hasViewers();
//        }
    }
}
