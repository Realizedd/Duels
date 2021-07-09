package me.realized.duels.hook.hooks.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldGuard7Handler implements WorldGuardHandler {

    @Override
    public String findRegion(final Player player, final Collection<String> regions) {
        final Location location = player.getLocation();
        final BlockVector3 vector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        for (final ProtectedRegion region : WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()))
                .getApplicableRegions(vector)) {
            if (regions.contains(region.getId())) {
                return region.getId();
            }
        }

        return null;
    }
}
