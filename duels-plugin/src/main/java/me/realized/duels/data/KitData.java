package me.realized.duels.data;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.kit.KitImpl.Characteristic;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitData {

    private String name;
    private ItemData displayed;
    private boolean usePermission;
    private boolean arenaSpecific;
    private Set<Characteristic> characteristics = new HashSet<>();
    private Map<String, Map<Integer, ItemData>> items = new HashMap<>();

    // for Gson
    private KitData() {
    }

    public KitData(final KitImpl kit) {
        this.name = kit.getName();
        this.displayed = new ItemData(kit.getDisplayed());
        this.usePermission = kit.isUsePermission();
        this.arenaSpecific = kit.isArenaSpecific();
        this.characteristics.addAll(kit.getCharacteristics());

        for (final Map.Entry<String, Map<Integer, ItemStack>> entry : kit.getItems().entrySet()) {
            final Map<Integer, ItemData> data = new HashMap<>();
            entry.getValue().entrySet().stream()
                    .filter(value -> Objects.nonNull(value.getValue()))
                    .forEach(value -> data.put(value.getKey(), new ItemData(value.getValue())));
            items.put(entry.getKey(), data);
        }
    }

    public KitImpl toKit(final DuelsPlugin plugin) {
        final KitImpl kit = new KitImpl(plugin, name, displayed.toItemStack(), usePermission, arenaSpecific, characteristics);

        for (final Map.Entry<String, Map<Integer, ItemData>> entry : items.entrySet()) {
            final Map<Integer, ItemStack> data = new HashMap<>();
            entry.getValue().forEach(((slot, itemData) -> data.put(slot, itemData.toItemStack())));
            kit.getItems().put(entry.getKey(), data);
        }

        return kit;
    }
}
