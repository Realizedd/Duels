package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.queue.DQueue;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.queue.sign.QueueSignImpl;
import me.realized.duels.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand extends BaseCommand {

    public ListCommand(final DuelsPlugin plugin) {
        super(plugin, "list", null, null, 1, false, "ls");
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final List<String> arenas = new ArrayList<>();
        arenaManager.getArenasImpl().forEach(arena -> arenas.add("&" + getColor(arena) + arena.getName()));
        final String kits = StringUtils.join(kitManager.getKits().stream().map(Kit::getName).collect(Collectors.toList()), ", ");
        final String queues = StringUtils.join(queueManager.getQueues().stream().map(DQueue::toString).collect(Collectors.toList()), ", ");
        final String signs = StringUtils.join(queueSignManager.getSigns().stream().map(QueueSignImpl::toString).collect(Collectors.toList()), ", ");
        lang.sendMessage(sender, "COMMAND.duels.list",
                "arenas", !arenas.isEmpty() ? StringUtils.join(arenas, "&r, &r") : lang.getMessage("GENERAL.none"),
                "kits", !kits.isEmpty() ? kits : lang.getMessage("GENERAL.none"),
                "queues", !queues.isEmpty() ? queues : lang.getMessage("GENERAL.none"),
                "queue_signs", !signs.isEmpty() ? signs : lang.getMessage("GENERAL.none"),
                "lobby", StringUtil.parse(playerManager.getLobby()));
    }

    private String getColor(final ArenaImpl arena) {
        return arena.isDisabled() ? "4" : (arena.getPositions().size() < 2 ? "9" : arena.isUsed() ? "c" : "a");
    }
}