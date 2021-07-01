package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.kit.KitImpl;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ResetratingCommand extends BaseCommand {

    public ResetratingCommand(final DuelsPlugin plugin) {
        super(plugin, "resetrating", "resetrating [name] [-:kit:all]", "Resets specified kit's rating or all.", 3, false, "resetr");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[1]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", args[1]);
            return;
        }

        if (args[2].equalsIgnoreCase("all")) {
            user.resetRating();
            kitManager.getKits().forEach(user::resetRating);
            lang.sendMessage(sender, "COMMAND.duels.reset-rating", "name", user.getName(), "kit", "all");
        } else if (args[2].equals("-")) {
            user.resetRating();
            lang.sendMessage(sender, "COMMAND.duels.reset-rating", "name", user.getName(), "kit", lang.getMessage("GENERAL.none"));
        } else {
            final String name = StringUtils.join(args, " ", 2, args.length).replace("-", " ");
            final KitImpl kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }

            user.resetRating(kit);
            lang.sendMessage(sender, "COMMAND.duels.reset-rating", "name", user.getName(), "kit", name);
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 3) {
            return handleTabCompletion(args[2], kitManager.getNames(true));
        }

        return null;
    }
}
