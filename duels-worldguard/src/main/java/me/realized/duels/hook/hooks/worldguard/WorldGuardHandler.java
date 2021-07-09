package me.realized.duels.hook.hooks.worldguard;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface WorldGuardHandler {

    String findRegion(final Player player, final Collection<String> regions);
}
