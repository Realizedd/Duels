package me.realized.duels.command.commands.duels.subcommands;

import java.util.Arrays;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DeletequeueCommand extends BaseCommand {

    public DeletequeueCommand(final DuelsPlugin plugin) {
        super(plugin, "deletequeue", "deletequeue [bet] [-:kit]", "Deletes a queue.", 3, false, "delqueue", "delq");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final double bet = NumberUtil.parseDouble(args[1]).orElse(0.0);
        KitImpl kit = null;

        if (!args[2].equals("-")) {
            String name = StringUtil.join(args, " ", 2, args.length).replace("-", " ");
            kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }
        }

        final String kitName = kit != null ? kit.getName() : lang.getMessage("GENERAL.none");

        if (queueManager.remove(sender, kit, bet) == null) {
            lang.sendMessage(sender, "ERROR.queue.not-found", "bet_amount", NumberUtil.formatDouble(bet), "kit", kitName);
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.delete-queue", "kit", kitName, "bet_amount", NumberUtil.formatDouble(bet));
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        if (args.length > 2) {
            return handleTabCompletion(args[2], kitManager.getNames(true));
        }

        return null;
    }
}
