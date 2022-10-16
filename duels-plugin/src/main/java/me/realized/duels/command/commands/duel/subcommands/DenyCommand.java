package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.request.RequestDenyEvent;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.request.RequestImpl;
import me.realized.duels.util.function.Pair;
import me.realized.duels.util.validator.ValidatorUtil;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenyCommand extends BaseCommand {

    public DenyCommand(final DuelsPlugin plugin) {
        super(plugin, "deny", "deny [player]", "Declines a duel request.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        if (!ValidatorUtil.validate(validatorManager.getDuelDenyTargetValidators(), new Pair<>(player, target), partyManager.get(target), Collections.emptyList())) {
            return;
        }
        
        final RequestImpl request = requestManager.remove(target, player);
        final RequestDenyEvent event = new RequestDenyEvent(player, target, request);
        Bukkit.getPluginManager().callEvent(event);

        if (request.isPartyDuel()) {
            final Collection<Player> senderPartyMembers = partyManager.getOnlinePlayers(request.getSenderParty());
            final Collection<Player> targetPartyMembers = partyManager.getOnlinePlayers(request.getTargetParty());
            lang.sendMessage(senderPartyMembers, "COMMAND.duel.party-request.deny.receiver-party", "owner", player.getName(), "name", target.getName());
            lang.sendMessage(targetPartyMembers, "COMMAND.duel.party-request.deny.sender-party", "owner", target.getName(), "name", player.getName());
        } else {
            lang.sendMessage(player, "COMMAND.duel.request.deny.receiver", "name", target.getName());
            lang.sendMessage(target, "COMMAND.duel.request.deny.sender", "name", player.getName());
        }
    }
}
