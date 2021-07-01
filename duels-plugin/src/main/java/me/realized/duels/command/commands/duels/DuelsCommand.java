package me.realized.duels.command.commands.duels;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duels.subcommands.*;
import org.bukkit.command.CommandSender;

public class DuelsCommand extends BaseCommand {

    public DuelsCommand(final DuelsPlugin plugin) {
        super(plugin, "duels", Permissions.ADMIN, false);
        child(
                new HelpCommand(plugin),
                new SavekitCommand(plugin),
                new DeletekitCommand(plugin),
                new LoadkitCommand(plugin),
                new SetitemCommand(plugin),
                new OptionsCommand(plugin),
                new BindCommand(plugin),
                new CreateCommand(plugin),
                new DeleteCommand(plugin),
                new SetCommand(plugin),
                new ToggleCommand(plugin),
                new TeleportCommand(plugin),
                new CreatequeueCommand(plugin),
                new DeletequeueCommand(plugin),
                new AddsignCommand(plugin),
                new DeletesignCommand(plugin),
                new SetlobbyCommand(plugin),
                new LobbyCommand(plugin),
                new InfoCommand(plugin),
                new ListCommand(plugin),
                new EditCommand(plugin),
                new SetratingCommand(plugin),
                new ResetCommand(plugin),
                new ResetratingCommand(plugin),
                new PlaysoundCommand(plugin),
                new ReloadCommand(plugin)
        );
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        lang.sendMessage(sender, "COMMAND.duels.usage", "command", label);
    }
}
