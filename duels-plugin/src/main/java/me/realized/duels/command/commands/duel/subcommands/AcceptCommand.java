package me.realized.duels.command.commands.duel.subcommands;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.request.RequestAcceptEvent;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import com.alessiodp.parties.api.interfaces.Party;
import me.realized.duels.request.RequestImpl;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.function.Pair;
import me.realized.duels.util.validator.ValidatorUtil;

public class AcceptCommand extends BaseCommand {

    private final WorldGuardHook worldGuard;

    public AcceptCommand(final DuelsPlugin plugin) {
        super(plugin, "accept", "accept [player]", "Accepts a duel request.", 2, true);
        this.worldGuard = hookManager.getHook(WorldGuardHook.class);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Party party = partyManager.get(player);
        final Collection<Player> players = party == null ? Collections.singleton(player) : partyManager.getOnlinePlayers(party);

        if (!ValidatorUtil.validate(validatorManager.getDuelAcceptSelfValidators(), player, party, players)) {
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        final Party targetParty = partyManager.get(target);
        final Collection<Player> targetPlayers = targetParty == null ? Collections.singleton(target) : partyManager.getOnlinePlayers(targetParty);

        if (!ValidatorUtil.validate(validatorManager.getDuelAcceptTargetValidators(), new Pair<>(player, target), targetParty, targetPlayers)) {
            return;
        }
        
        final RequestImpl request = requestManager.remove(target, player);
        final RequestAcceptEvent event = new RequestAcceptEvent(player, target, request);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        final Settings settings = request.getSettings();
        final String kit = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected");
        final String ownInventory = settings.isOwnInventory() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");

        if (request.isPartyDuel()) {
            final Collection<Player> senderPartyMembers = partyManager.getOnlinePlayers(request.getSenderParty());
            final Collection<Player> targetPartyMembers = partyManager.getOnlinePlayers(request.getTargetParty());
            lang.sendMessage(senderPartyMembers, "COMMAND.duel.party-request.accept.receiver-party",
            "owner", player.getName(), "name", target.getName(), "kit", kit, "own_inventory", ownInventory, "arena", arena);
            lang.sendMessage(targetPartyMembers, "COMMAND.duel.party-request.accept.sender-party",
            "owner", target.getName(), "name", player.getName(), "kit", kit, "own_inventory", ownInventory, "arena", arena);
        } else {
            final double bet = settings.getBet();
            final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
            lang.sendMessage(player, "COMMAND.duel.request.accept.receiver",
                "name", target.getName(), "kit", kit, "arena", arena, "bet_amount", bet, "item_betting", itemBetting);
            lang.sendMessage(target, "COMMAND.duel.request.accept.sender",
                "name", player.getName(), "kit", kit, "arena", arena, "bet_amount", bet, "item_betting", itemBetting);
        }

        if (settings.isItemBetting()) {
            players.forEach(all -> {
                settings.setBaseLoc(all);
                settings.setDuelzone(all, worldGuard != null ? worldGuard.findDuelZone(all) : null);
            });
            bettingManager.open(settings, target, player);
        } else {
            duelManager.startMatch(target, player, settings, null, null);
        }
    }
}
