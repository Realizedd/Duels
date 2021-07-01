package me.realized.duels.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public final class BlockUtil {

    private BlockUtil() {
    }

    public static <T extends BlockState> T getTargetBlock(final Player player, final Class<T> type, final int range) {
        final BlockIterator iterator = new BlockIterator(player, range);

        while (iterator.hasNext()) {
            final Block block = iterator.next();

            if (type.isInstance(block.getState())) {
                return type.cast(block.getState());
            }
        }

        return null;
    }
}
