package me.realized.duels.betting;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.gui.betting.BettingGui;
import me.realized.duels.hook.hooks.VaultHook;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.gui.GuiListener;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class BettingManager implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final GuiListener<DuelsPlugin> guiListener;

    public BettingManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.guiListener = plugin.getGuiListener();
    }

    @Override
    public void handleLoad() {
        final VaultHook vaultHook = plugin.getHookManager().getHook(VaultHook.class);

        if (vaultHook == null) {
            Log.info(this, "Vault was not found! Money betting feature will be automatically disabled.");
        } else if (vaultHook.getEconomy() == null) {
            Log.info(this, "Economy plugin supporting Vault was not found! Money betting feature will be automatically disabled.");
        }
    }

    @Override
    public void handleUnload() {
    }

    public void open(final Settings settings, final Player first, final Player second) {
        final BettingGui gui = new BettingGui(plugin, settings, first, second);
        guiListener.addGui(first, gui).open(first);
        guiListener.addGui(second, gui).open(second);
    }
}
