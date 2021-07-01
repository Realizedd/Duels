package me.realized.duels.hook.hooks.worldguard;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.util.compat.ReflectionUtil;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldGuardHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "WorldGuard";

    private final Config config;
    private final WorldGuardHandler handler;

    public WorldGuardHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.handler = ReflectionUtil.getClassUnsafe("com.sk89q.worldguard.WorldGuard") != null ? new WorldGuard7Handler() : new WorldGuard6Handler();
    }

    public String findDuelZone(final Player player) {
        if (!config.isDuelzoneEnabled()) {
            return null;
        }

        final Collection<String> allowedRegions = config.getDuelzones();

        if (allowedRegions.isEmpty()) {
            return null;
        }

        return handler.findRegion(player, allowedRegions);
    }
}
