package me.realized.duels.util.inventory;

import java.util.function.Consumer;

public final class Slots {

    private Slots() {
    }

    public static void run(final int from, final int to, final int height, final Consumer<Integer> action) {
        for (int h = 0; h < height; h++) {
            for (int slot = from; slot < to; slot++) {
                action.accept(slot + h * 9);
            }
        }
    }

    public static void run(final int from, final int to, final Consumer<Integer> action) {
        for (int slot = from; slot < to; slot++) {
            action.accept(slot);
        }
    }
}
