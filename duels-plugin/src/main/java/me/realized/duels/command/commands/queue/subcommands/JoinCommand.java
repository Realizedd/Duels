package me.realized.duels.command.commands.queue.subcommands;

import java.util.Arrays;
import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.queue.Queue;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand extends BaseCommand {

    public JoinCommand(final DuelsPlugin plugin) {
        super(plugin, "join", "join [-:kit] [bet]", "Joins a queue.", Permissions.QUEUE, 2, true, "j");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;
        KitImpl kit = null;

        if (!args[1].startsWith("-")) {
            String name = StringUtil.join(args, " ", 1, args.length - (args.length > 2 ? 1 : 0)).replace("-", " ");
            kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }
        }

        final String kitName = kit != null ? kit.getName() : lang.getMessage("GENERAL.none");
        final double bet = args.length > 2 ? NumberUtil.parseDouble(args[args.length - 1]).orElse(0) : 0;
        final Queue queue = args[1].equals("-r") ? queueManager.randomQueue() : queueManager.get(kit, bet);

        if (queue == null) {
            lang.sendMessage(sender, "ERROR.queue.not-found", "bet_amount", NumberUtil.formatDouble(bet), "kit", kitName);
            return;
        }

        queueManager.queue(player, queue);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], kitManager.getNames(true));
        }

        if (args.length > 2) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        return null;
    }
}
