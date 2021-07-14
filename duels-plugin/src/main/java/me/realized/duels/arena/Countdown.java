package me.realized.duels.arena;

import com.cryptomorin.xseries.messages.Titles;
import com.google.common.collect.Lists;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.duel.DuelManager.OpponentInfo;
import me.realized.duels.util.StringUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

class Countdown extends BukkitRunnable {

    private final Config config;
    private final ArenaImpl arena;
    private final String kit;
    private final Map<UUID, OpponentInfo> info;
    private final List<String> messages;
    private final List<String> titles;

    private boolean finished;

    Countdown(final DuelsPlugin plugin, final ArenaImpl arena, final String kit, final Map<UUID, OpponentInfo> info, final List<String> messages, final List<String> titles) {
        this.config = plugin.getConfiguration();
        this.arena = arena;
        this.kit = kit;
        this.info = info;
        this.messages = Lists.newArrayList(messages);
        this.titles = Lists.newArrayList(titles);
    }

    @Override
    public void run() {
        if (finished) {
            return;
        }
        if (!messages.isEmpty()) {
            final String rawMessage = messages.remove(0);
            final String message = StringUtil.color(rawMessage);
            final String title = !titles.isEmpty() ? titles.remove(0) : null;

            arena.getPlayers().forEach(player -> {
                config.playSound(player, rawMessage);

                final OpponentInfo info = this.info.get(player.getUniqueId());

                if (info != null) {
                    player.sendMessage(message
                            .replace("%opponent%", info.getName())
                            .replace("%opponent_rating%", String.valueOf(info.getRating()))
                            .replace("%kit%", kit)
                            .replace("%arena%", arena.getName())
                    );
                } else {
                    player.sendMessage(message);
                }

                if (title != null) {
                    Titles.sendTitle(player, 0, 20, 50, StringUtil.color(title), null);
                }
            });
        }

        if (!arena.isUsed() || messages.isEmpty()) {
            arena.setCountdown(null);
            cancel();
            finished = true;
        }
    }
}
