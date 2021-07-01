package me.realized.duels.hook.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManagerImpl;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.OfflinePlayer;

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
        public String getPlugin() {
            return plugin.getName();
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
        public String onRequest(final OfflinePlayer player, final String identifier) {
            if (player == null) {
                return "Player is required";
            }

            final UserData user = userDataManager.get(player.getUniqueId());

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
            }

            return null;
        }
    }
}
