package me.realized.duels.util.compat;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public final class Panes {

    private static final Map<Short, String> DATA_TO_PANE = new HashMap<>();

    static {
        DATA_TO_PANE.put((short) 0, "WHITE_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 1, "ORANGE_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 2, "MAGENTA_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 3, "LIGHT_BLUE_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 4, "YELLOW_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 5, "LIME_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 6, "PINK_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 7, "GRAY_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 8, "LIGHT_GRAY_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 9, "CYAN_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 10, "PURPLE_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 11, "BLUE_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 12, "BROWN_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 13, "GREEN_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 14, "RED_STAINED_GLASS_PANE");
        DATA_TO_PANE.put((short) 15, "BLACK_STAINED_GLASS_PANE");
    }

    private Panes() {
    }

    public static Material from(final short data) {
        return Material.getMaterial(DATA_TO_PANE.get(data));
    }
}
