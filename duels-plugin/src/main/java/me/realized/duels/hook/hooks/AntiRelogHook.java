package me.realized.duels.hook.hooks;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.util.hook.PluginHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.leymooo.antirelog.Antirelog;
import ru.leymooo.antirelog.event.PvpPreStartEvent;
import ru.leymooo.antirelog.event.PvpStartedEvent;

public class AntiRelogHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "AntiRelog";

    private final Config config;
    private final ArenaManagerImpl arenaManager;

    public AntiRelogHook(final DuelsPlugin plugin) {
        super(plugin, NAME);
        this.config = plugin.getConfiguration();
        this.arenaManager = plugin.getArenaManager();

        try {
            Class.forName("ru.leymooo.antirelog.event.PvpPreStartEvent");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("This version of " + getName() + " is not supported. Please try upgrading to the latest version.");
        }

        Bukkit.getPluginManager().registerEvents(new AntiRelogHook.AntiRelogListener(), plugin);
    }

    public boolean isTagged(final Player player) {
        return config.isArPreventDuel() && ((Antirelog) getPlugin()).getPvpManager().isInPvP(player);
    }

    public class AntiRelogListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void on(final PvpPreStartEvent event) {
            if (!config.isArPreventTag()) {
                return;
            }

            final Player attacker = event.getAttacker();
            final Player defender = event.getDefender();

            if (!arenaManager.isInMatch(attacker) && !arenaManager.isInMatch(defender)) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
