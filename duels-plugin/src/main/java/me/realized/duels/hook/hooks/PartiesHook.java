package me.realized.duels.hook.hooks;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.Log;
import me.realized.duels.util.hook.PluginHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;
import java.util.Collection;

public class PartiesHook extends PluginHook<DuelsPlugin> {

    public static final String NAME = "Parties";

    @Getter
    private PartiesAPI api;

    public PartiesHook(final DuelsPlugin plugin) {
        super(plugin, NAME);

        Plugin p = Bukkit.getPluginManager().getPlugin("Parties");

        if (p == null) {
            Log.warn("Parties not found.");
            return;
        }

        if (!p.isEnabled()) {
            Log.warn("Parties not enabled.");
            return;
        }

        api = Parties.getApi();
        Log.info("Using Parties: " + api.getClass().getName());
    }

}
