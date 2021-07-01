package me.realized.duels.data;

import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.kit.KitImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArenaData {

    @Getter
    private String name;
    private boolean disabled;
    private Set<String> kits = new HashSet<>();
    private Map<Integer, LocationData> positions = new HashMap<>();

    // for Gson
    private ArenaData() {
    }

    public ArenaData(final ArenaImpl arena) {
        this.name = arena.getName();
        this.disabled = arena.isDisabled();
        arena.getKits().forEach(kit -> this.kits.add(kit.getName()));
        arena.getPositions().entrySet()
                .stream().filter(entry -> entry.getValue().getWorld() != null).forEach(entry -> positions.put(entry.getKey(), new LocationData(entry.getValue())));
    }

    public ArenaImpl toArena(final DuelsPlugin plugin) {
        final ArenaImpl arena = new ArenaImpl(plugin, name);
        arena.setDisabled(disabled);

        for (final String name : kits) {
            final KitImpl kit = plugin.getKitManager().get(name);

            if (kit == null) {
                continue;
            }

            arena.bind(kit);
        }

        for (final Map.Entry<Integer, LocationData> entry : positions.entrySet()) {
            arena.setPosition(entry.getKey(), entry.getValue().toLocation());
        }

        return arena;
    }
}
