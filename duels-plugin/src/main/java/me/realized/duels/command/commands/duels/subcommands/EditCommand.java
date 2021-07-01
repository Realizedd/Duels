package me.realized.duels.command.commands.duels.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.user.User;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.TriFunction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EditCommand extends BaseCommand {

    private final Map<String, Function<User, Integer>> getters = new HashMap<>();
    private final Map<String, BiConsumer<User, Integer>> setters = new HashMap<>();
    private final Map<String, TriFunction<User, String, Integer, Integer>> actions = new HashMap<>();

    public EditCommand(final DuelsPlugin plugin) {
        super(plugin, "edit", "edit [name] [add:remove:set] [wins:losses] [amount]", "Edits player's wins or losses.", 5, false);
        getters.put("wins", User::getWins);
        getters.put("losses", User::getLosses);
        setters.put("wins", User::setWins);
        setters.put("losses", User::setLosses);
        actions.put("set", (user, type, amount) -> amount);
        actions.put("add", (user, type, amount) -> getters.get(type).apply(user) + amount);
        actions.put("remove", (user, type, amount) -> getters.get(type).apply(user) - amount);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final UserData user = userManager.get(args[1]);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", args[1]);
            return;
        }

        final String actionName = args[2].toLowerCase();
        final TriFunction<User, String, Integer, Integer> action = actions.get(actionName);

        if (action == null) {
            lang.sendMessage(sender, "ERROR.command.invalid-action", "action", actionName, "available_actions", actions.keySet());
            return;
        }

        final String option = args[3].toLowerCase();
        final BiConsumer<User, Integer> setter = setters.get(option);

        if (setter == null) {
            lang.sendMessage(sender, "ERROR.command.invalid-option", "option", option, "available_options", getters.keySet());
            return;
        }

        final int amount = Math.max(NumberUtil.parseInt(args[4]).orElse(0), 0);
        setter.accept(user, action.apply(user, option, amount));
        lang.sendMessage(sender, "COMMAND.duels.edit", "name", user.getName(), "type", option, "action", actionName, "amount", amount);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 3) {
            return actions.keySet().stream()
                    .filter(type -> type.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 4) {
            return setters.keySet().stream()
                    .filter(type -> type.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length > 4) {
            return Arrays.asList("0", "10", "50", "100", "500", "1000");
        }

        return null;
    }
}
