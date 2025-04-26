package me.realized.duels.hook.hooks;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CombatLogXHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "CombatLogX";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public CombatLogXHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try {
            Class.forName("com.github.sirblobman.combatlogx.api.event.PlayerPreTagEvent");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }

        Bukkit.getPluginManager().registerEvents(new CombatLogXListener(), plugin);
    }

    public boolean isTagged(final Player player) {
        return config.isClxPreventDuel() && ((ICombatLogX) getPlugin()).getCombatManager().isInCombat(player);
    }

    public class CombatLogXListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerPreTagEvent event) {
            if (!config.isClxPreventTag()) {
                return;
            }

            final Player player = event.getPlayer();

            if (!arenaManager.isInMatch(player)) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
