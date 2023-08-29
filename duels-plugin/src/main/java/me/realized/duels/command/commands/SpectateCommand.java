package me.realized.duels.command.commands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.spectate.SpectateManager.Result;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.MatchImpl;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.spectate.SpectatorImpl;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand extends BaseCommand {

    public SpectateCommand(final DuelsPlugin plugin) {
        super(plugin, "spectate", Permissions.SPECTATE, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        final SpectatorImpl spectator = spectateManager.get(player);

        // If player is already spectating, using /spectate will put them out of spectator mode.
        if (spectator != null) {
            spectateManager.stopSpectating(player);
            lang.sendMessage(player, "COMMAND.spectate.stop-spectate", "name", spectator.getTargetName());
            return;
        }

        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.spectate.usage", "command", label);
            return;
        }

        if (config.isSpecRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(sender, "ERROR.duel.inventory-not-empty");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return;
        }

        final Result result = spectateManager.startSpectating(player, target);

        switch (result) {
            case EVENT_CANCELLED:
                return;
            case IN_MATCH:
                lang.sendMessage(player, "ERROR.spectate.already-spectating.sender");
                return;
            case IN_QUEUE:
                lang.sendMessage(player, "ERROR.duel.already-in-queue");
                return;
            case ALREADY_SPECTATING:
                lang.sendMessage(player, "ERROR.duel.already-in-match.sender");
                return;
            case TARGET_NOT_IN_MATCH:
                lang.sendMessage(player, "ERROR.spectate.not-in-match", "name", target.getName());
                return;
            case SUCCESS:
                final ArenaImpl arena = arenaManager.get(target);

                // Meaningless checks to halt IDE warnings as target is guaranteed to be in a match if result is SUCCESS.
                if (arena == null || arena.getMatch() == null) {
                    return;
                }

                final MatchImpl match = arena.getMatch();
                final String kit = match.getKit() != null ? match.getKit().getName() : lang.getMessage("GENERAL.none");
                lang.sendMessage(player, "COMMAND.spectate.start-spectate",
                    "name", target.getName(),
                    "opponent", arena.getOpponent(target).getName(),
                    "kit", kit,
                    "arena", arena.getName(),
                    "bet_amount", NumberUtil.formatDouble(match.getBet())
                );
        }
    }
}
