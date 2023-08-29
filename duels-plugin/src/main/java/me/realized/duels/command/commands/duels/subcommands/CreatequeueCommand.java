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

public class CreatequeueCommand extends BaseCommand {

    public CreatequeueCommand(final DuelsPlugin plugin) {
        super(plugin, "createqueue", "createqueue [bet] [-:kit]", "Creates a queue with given bet and kit.", 3, false, "createq");
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

        if (queueManager.create(sender, kit, bet) == null) {
            lang.sendMessage(sender, "ERROR.queue.already-exists", "kit", kitName, "bet_amount", NumberUtil.formatDouble(bet));
            return;
        }

        lang.sendMessage(sender, "COMMAND.duels.create-queue", "kit", kitName, "bet_amount", NumberUtil.formatDouble(bet));
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
