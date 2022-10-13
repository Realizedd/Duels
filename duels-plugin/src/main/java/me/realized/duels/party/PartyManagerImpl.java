package me.realized.duels.party;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Lang;
import me.realized.duels.hook.hooks.PartiesHook;
import me.realized.duels.util.EventUtil;
import me.realized.duels.util.Loadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collection;
import java.util.stream.Collectors;

public class PartyManagerImpl implements Loadable, Listener {

    private final DuelsPlugin plugin;
    private final Lang lang;
    private final PartiesAPI partiesAPI;

    public PartyManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.partiesAPI = plugin.getHookManager().getHook(PartiesHook.class).getApi();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() {

    }

    @Override
    public void handleUnload() {

    }

    public com.alessiodp.parties.api.interfaces.Party get(final Player player) {
        return partiesAPI.getPartyOfPlayer(player.getUniqueId());
    }

    public Collection<Player> getOnlinePlayers(com.alessiodp.parties.api.interfaces.Party party) {
        return party.getOnlineMembers().stream().map(partyPlayer -> plugin.getServer().getPlayer(partyPlayer.getPlayerUUID())).collect(Collectors.toList());
    }

    public boolean canDamage(final Player damager, final Player damaged) {
        final com.alessiodp.parties.api.interfaces.Party party = get(damager);

        if (party == null || !party.equals(get(damaged))) {
            return true;
        }

        return party.isFriendlyFireProtected();
    }

    @EventHandler
    public void on(final EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final Player damaged = (Player) event.getEntity();
        final Player damager = EventUtil.getDamager(event);

        if (damager == null || canDamage(damager, damaged)) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(damager, "ERROR.party.cannot-friendly-fire", "name", damaged.getName());
    }
}
