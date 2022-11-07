package me.realized.duels.hook.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EssentialsHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "Essentials";

    private final Config config;

    public EssentialsHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
    }

    public boolean isVanished(@NotNull Player player) {
        return getEssentials().getUser(player).isVanished();
    }

    public void tryUnvanish(final Player player) {
        if (!config.isAutoUnvanish()) {
            return;
        }

        final User user = getEssentials().getUser(player);
        if (user != null && user.isVanished()) {
            user.setVanished(false);
        }
    }

    public void setBackLocation(final Player player, final Location location) {
        if (!config.isSetBackLocation()) {
            return;
        }

        final User user = getEssentials().getUser(player);
        if (user != null) {
            user.setLastLocation(location);
        }
    }

    private Essentials getEssentials() {
        return (Essentials) getPlugin();
    }
}
