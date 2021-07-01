package me.realized.duels.queue.sign;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.api.queue.sign.QueueSign;
import me.realized.duels.queue.Queue;
import me.realized.duels.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.Objects;

public class QueueSignImpl implements QueueSign {

    @Getter
    private final Location location;
    @Getter
    private final String[] lines;
    @Getter
    private final Queue queue;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    private int lastInQueue;
    private long lastInMatch;

    public QueueSignImpl(final Location location, final String format, final Queue queue) {
        this.location = location;
        this.queue = queue;

        final String[] data = {"", "", "", ""};

        if (format != null) {
            final String[] lines = format.split("\n");
            System.arraycopy(lines, 0, data, 0, lines.length);
        }

        this.lines = data;

        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        final Sign sign = (Sign) block.getState();

        sign.setLine(0, replace(lines[0], 0, 0));
        sign.setLine(1, replace(lines[1], 0, 0));
        sign.setLine(2, replace(lines[2], 0, 0));
        sign.setLine(3, replace(lines[3], 0, 0));
        sign.update(true);
    }

    private String replace(final String line, final int inQueue, final long inMatch) {
        return StringUtil.color(line.replace("%in_queue%", String.valueOf(inQueue)).replace("%in_match%", String.valueOf(inMatch)));
    }

    public void update() {
        final Block block = location.getBlock();

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        final Sign sign = (Sign) block.getState();

        if (queue.isRemoved()) {
            sign.setType(Material.AIR);
            sign.update(true);
            return;
        }

        final int inQueue = queue.getPlayers().size();
        final long inMatch = queue.getPlayersInMatch();

        if (lastInQueue == inQueue && lastInMatch == inMatch) {
            return;
        }

        this.lastInQueue = inQueue;
        this.lastInMatch = inMatch;

        sign.setLine(0, replace(lines[0], inQueue, inMatch));
        sign.setLine(1, replace(lines[1], inQueue, inMatch));
        sign.setLine(2, replace(lines[2], inQueue, inMatch));
        sign.setLine(3, replace(lines[3], inQueue, inMatch));
        sign.update(true);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final QueueSignImpl queueSign = (QueueSignImpl) o;
        return Objects.equals(queue, queueSign.getQueue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(queue);
    }

    @Override
    public String toString() {
        return StringUtil.parse(location);
    }
}
