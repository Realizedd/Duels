package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class InfoCommand extends BaseCommand {

    public InfoCommand(final DuelsPlugin plugin) {
        super(plugin, "info", "info [name]", "Displays information about the selected arena.", 2, false);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final String name = StringUtils.join(args, " ", 1, args.length).replace("-", " ");
        final ArenaImpl arena = arenaManager.get(name);

        if (arena == null) {
            lang.sendMessage(sender, "ERROR.arena.not-found", "name", name);
            return;
        }

        final String inUse = arena.isUsed() ? lang.getMessage("GENERAL.true") : lang.getMessage("GENERAL.false");
        final String disabled = arena.isDisabled() ? lang.getMessage("GENERAL.true") : lang.getMessage("GENERAL.false");
        final String kits = StringUtils.join(arena.getKits().stream().map(KitImpl::getName).collect(Collectors.toList()), ", ");
        final String positions = StringUtils.join(arena.getPositions().values().stream().map(StringUtil::parse).collect(Collectors.toList()), ", ");
        final String players = StringUtils.join(arena.getPlayers().stream().map(Player::getName).collect(Collectors.toList()), ", ");
        lang.sendMessage(sender, "COMMAND.duels.info", "name", name, "in_use", inUse, "disabled", disabled, "kits",
                !kits.isEmpty() ? kits : lang.getMessage("GENERAL.none"), "positions", !positions.isEmpty() ? positions : lang.getMessage("GENERAL.none"), "players",
                !players.isEmpty() ? players : lang.getMessage("GENERAL.none"));
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 2) {
            return handleTabCompletion(args[1], arenaManager.getNames());
        }

        return null;
    }
}
