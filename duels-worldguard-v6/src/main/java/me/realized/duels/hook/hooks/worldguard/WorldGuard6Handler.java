package me.realized.duels.hook.hooks.worldguard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldGuard6Handler implements WorldGuardHandler {

    @Override
    public String findRegion(final Player player, final Collection<String> regions) {
        for (final ProtectedRegion region : WorldGuardPlugin.inst().getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation())) {
            if (regions.contains(region.getId())) {
                return region.getId();
            }
        }

        return null;
    }
}
