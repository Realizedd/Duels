package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.user.UserManager.TopData;
import me.realized.duels.api.user.UserManager.TopEntry;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.KitImpl;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class TopCommand extends BaseCommand {

    public TopCommand(final DuelsPlugin plugin) {
        super(plugin, "top", "top [-:kit:wins:losses]", "Displays top wins, losses, or rating for kit.", Permissions.TOP, 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        if (!userManager.isLoaded()) {
            lang.sendMessage(sender, "ERROR.data.not-loaded");
            return;
        }

        final TopEntry topEntry;

        if (args[1].equals("-")) {
            topEntry = userManager.getTopRatings();
        } else if (args[1].equalsIgnoreCase("wins")) {
            topEntry = userManager.getWins();
        } else if (args[1].equalsIgnoreCase("losses")) {
            topEntry = userManager.getLosses();
        } else {
            final String name = StringUtils.join(args, " ", 1, args.length);
            final KitImpl kit = kitManager.get(name);

            if (kit == null) {
                lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                return;
            }

            topEntry = userManager.getTopRatings(kit);
        }

        final List<TopData> top;

        if (topEntry == null || (top = topEntry.getData()).isEmpty()) {
            lang.sendMessage(sender, "ERROR.top.no-data-available");
            return;
        }

        lang.sendMessage(sender, "COMMAND.duel.top.next-update", "remaining", userManager.getNextUpdate(topEntry.getCreation()));
        lang.sendMessage(sender, "COMMAND.duel.top.header", "type", topEntry.getType());

        for (int i = 0; i < top.size(); i++) {
            final TopData data = top.get(i);
            lang.sendMessage(sender, "COMMAND.duel.top.display-format",
                    "rank", i + 1, "name", data.getName(), "score", data.getValue(), "identifier", topEntry.getIdentifier());
        }

        lang.sendMessage(sender, "COMMAND.duel.top.footer", "type", topEntry.getType());
    }
}
