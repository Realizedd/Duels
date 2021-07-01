package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.util.DateUtil;
import me.realized.duels.util.TextBuilder;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class StatsCommand extends BaseCommand {

    public StatsCommand(final DuelsPlugin plugin) {
        super(plugin, "stats", null, null, Permissions.STATS, 1, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (args.length > getLength()) {
            if (!sender.hasPermission(Permissions.STATS_OTHERS)) {
                lang.sendMessage(sender, "ERROR.no-permission", "permission", Permissions.STATS_OTHERS);
                return;
            }

            displayStats(player, args[1]);
            return;
        }

        displayStats(player, player.getName());
    }

    private void displayStats(final Player sender, final String name) {
        final UserData user = userManager.get(name);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", name);
            return;
        }

        final String wins = String.valueOf(user.getWins());
        final String losses = String.valueOf(user.getLosses());
        final String wlRatio = String.valueOf(user.getLosses() > 0 ? Math.round(((double) user.getWins() / (double) user.getLosses()) * 100.0) / 100.0 : user.getWins());
        final String requests = String.valueOf(user.canRequest() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled"));
        final Object[] args = {"name", user.getName(), "wins", wins, "losses", losses, "wl_ratio", wlRatio, "requests_enabled", requests};
        lang.sendMessage(sender, "COMMAND.duel.stats.displayed", args);

        if (config.isDisplayKitRatings() || config.isDisplayNoKitRating()) {
            lang.sendMessage(sender, "COMMAND.duel.stats.rating.header", args);

            if (config.isDisplayNoKitRating()) {
                lang.sendMessage(sender, "COMMAND.duel.stats.rating.format",
                        "type", config.getTopNoKitType(), "kit", config.getTopNoKitType(), "rating", user.getRating());
            }

            if (config.isDisplayKitRatings()) {
                kitManager.getKits().forEach(kit -> lang.sendMessage(sender, "COMMAND.duel.stats.rating.format",
                        "type", kit.getName(), "kit", kit.getName(), "rating", user.getRating(kit)));
            }

            lang.sendMessage(sender, "COMMAND.duel.stats.rating.footer", args);
        }

        if (config.isDisplayPastMatches()) {
            lang.sendMessage(sender, "COMMAND.duel.stats.match.header", args);

            final Calendar calendar = new GregorianCalendar();

            user.getMatches().forEach(match -> {
                final String kit = match.getKit() != null ? match.getKit() : lang.getMessage("GENERAL.none");
                final String duration = DateUtil.formatMilliseconds(match.getDuration());
                final String timeSince = DateUtil.formatMilliseconds(calendar.getTimeInMillis() - match.getCreation());
                TextBuilder
                        .of(lang.getMessage("COMMAND.duel.stats.match.format", "winner", match.getWinner(), "loser", match.getLoser()))
                        .setHoverEvent(Action.SHOW_TEXT,
                                lang.getMessage("COMMAND.duel.stats.match.hover-text",
                                        "kit", kit, "duration", duration, "time", timeSince, "health", match.getHealth()))
                        .send(sender);
            });
            lang.sendMessage(sender, "COMMAND.duel.stats.match.footer", args);
        }
    }
}
