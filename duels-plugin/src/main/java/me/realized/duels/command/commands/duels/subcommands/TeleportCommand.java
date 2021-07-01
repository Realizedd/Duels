package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.command.BaseCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class TeleportCommand extends BaseCommand {

    public TeleportCommand(final DuelsPlugin plugin) {
        super(plugin, "teleport", "teleport [name] <-2>", "Teleports to an arena.", 2, true, "tp", "goto");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final boolean second = args[args.length - 1].equals("-2");
        final String name = StringUtils.join(args, " ", 1, args.length - (second ? 1 : 0)).replace("-", " ");
        final ArenaImpl arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", name);
            return;
        }

        if (arena.getPositions().isEmpty()) {
            lang.sendMessage(sender, "ERROR.arena.no-position-set", "name", name);
            return;
        }

        final int pos = second ? 2 : 1;
        final Location location = arena.getPosition(pos);

        if (location == null) {
            lang.sendMessage(sender, "ERROR.arena.invalid-position");
            return;
        }

        ((Player) sender).teleport(location);
        lang.sendMessage(sender, "COMMAND.duels.teleport", "name", name, "position", pos);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], arenaManager.getNames());
        }

        return null;
    }
}
