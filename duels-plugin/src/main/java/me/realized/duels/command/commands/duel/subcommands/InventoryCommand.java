package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.gui.inventory.InventoryGui;
import me.realized.duels.util.UUIDUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class InventoryCommand extends BaseCommand {

    public InventoryCommand(final DuelsPlugin plugin) {
        super(plugin, "_", "_ [uuid]", "Displays player's inventories after match.", 2, true);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UUID target = UUIDUtil.parseUUID(args[1]);

        if (target == null) {
            lang.sendMessage(sender, "ERROR.inventory-view.not-a-uuid", "input", args[1]);
            return;
        }

        final InventoryGui gui = inventoryManager.get(UUID.fromString(args[1]));

        if (gui == null) {
            lang.sendMessage(sender, "ERROR.inventory-view.not-found", "uuid", target);
            return;
        }

        gui.open((Player) sender);
    }
}
