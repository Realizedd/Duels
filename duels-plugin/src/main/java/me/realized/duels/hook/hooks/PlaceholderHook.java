package me.realized.duels.hook.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManagerImpl;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlaceholderHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "PlaceholderAPI";

    private final UserManagerImpl userDataManager;

    public PlaceholderHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.userDataManager = plugin.getUserManager();
        new Placeholders().register();
    }

    public class Placeholders extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "duels";
        }

        @Override
        public String getAuthor() {
            return "Realized";
        }

        @Override
        public String getVersion() {
            return "1.0";
        }

        @Override
        public boolean persist() {
            return true;
        }

        @Override
        public String onPlaceholderRequest(final Player player, final String identifier) {
            if (player == null) {
                return "Player is required";
            }

            final UserData user = userDataManager.get(player);

            if (user == null) {
                return null;
            }

            switch (identifier) {
                case "wins":
                    return String.valueOf(user.getWins());
                case "losses":
                    return String.valueOf(user.getLosses());
                case "can_request":
                    return String.valueOf(user.canRequest());

                case "hits": {
                    final ArenaManagerImpl arenaManager = plugin.getArenaManager();
                    final ArenaImpl arena = arenaManager.get(player);

                    // Only activate when winner is undeclared
                    if (arena == null || !arenaManager.isInMatch(player) || arena.isEndGame()) {
                        return "-1";
                    }

                    return String.valueOf(arena.getMatch().getHits(player));
                }

                case "hits_opponent": {
                    final ArenaManagerImpl arenaManager = plugin.getArenaManager();
                    final ArenaImpl arena = arenaManager.get(player);

                    // Only activate when winner is undeclared
                    if (arena == null || !arenaManager.isInMatch(player) || arena.isEndGame()) {
                        return "-1";
                    }

                    return String.valueOf(arena.getMatch().getHits(arena.getOpponent(player)));
                }

                case "hits_diff": {
                    final ArenaManagerImpl arenaManager = plugin.getArenaManager();
                    final ArenaImpl arena = arenaManager.get(player);

                    // Only activate when winner is undeclared
                    if (arena == null || !arenaManager.isInMatch(player) || arena.isEndGame()) {
                        return "-1";
                    }

                    int playerHits = arena.getMatch().getHits(player);
                    int opponentHits = arena.getMatch().getHits(arena.getOpponent(player));

                    int hitsDiff = playerHits - opponentHits;

                    String color = hitsDiff == 0? "&7" : hitsDiff > 0? "&a+" : "&c";

                    return StringUtil.color(color) + hitsDiff;
                }

            }

            return null;
        }
    }
}
