package me.realized.duels.util.command;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.realized.duels.api.command.SubCommand;
import me.realized.duels.util.StringUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractCommand<P extends JavaPlugin> implements TabCompleter {

    protected final P plugin;

    @Getter
    private final String name;
    @Getter
    private final String usage;
    @Getter
    private final String description;
    @Getter
    private final String permission;
    @Getter
    private final boolean playerOnly;
    @Getter
    private final int length;
    @Getter
    private final List<String> aliases;

    private Map<String, AbstractCommand<P>> children;

    protected AbstractCommand(final P plugin, final String name, final String usage, final String description, final String permission, final int length,
                              final boolean playerOnly, final String... aliases) {
        this.plugin = plugin;
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.permission = permission;
        this.length = length;
        this.playerOnly = playerOnly;

        final List<String> names = Lists.newArrayList(aliases);
        names.add(name);

        this.aliases = Collections.unmodifiableList(names);
    }

    protected AbstractCommand(final P plugin, final SubCommand sub) {
        this(plugin, sub.getName(), sub.getUsage(), sub.getDescription(), sub.getPermission(), sub.getLength(), sub.isPlayerOnly(), sub.getAliases());
    }

    @SafeVarargs
    public final void child(final AbstractCommand<P>... commands) {
        if (commands == null || commands.length == 0) {
            return;
        }

        if (children == null) {
            children = new HashMap<>();
        }

        for (final AbstractCommand<P> child : commands) {
            for (String alias : child.getAliases()) {
                alias = alias.toLowerCase();

                if (children.containsKey(alias)) {
                    continue;
                }

                children.put(alias, child);
            }
        }
    }

    public boolean isChild(final String name) {
        return children != null && children.get(name.toLowerCase()) != null;
    }

    private PluginCommand getCommand() {
        final PluginCommand pluginCommand = plugin.getCommand(name);

        if (pluginCommand == null) {
            throw new IllegalArgumentException("Command is not registered in plugin.yml");
        }

        return pluginCommand;
    }

    public final void register() {
        final PluginCommand pluginCommand = getCommand();

        pluginCommand.setExecutor((sender, command, label, args) -> {
            if (isPlayerOnly() && !(sender instanceof Player)) {
                handleMessage(sender, MessageType.PLAYER_ONLY);
                return true;
            }

            if (permission != null && !sender.hasPermission(getPermission())) {
                handleMessage(sender, MessageType.NO_PERMISSION, permission);
                return true;
            }

            if (executeFirst(sender, label, args)) {
                return true;
            }

            if (args.length > 0 && children != null) {
                final AbstractCommand<P> child = children.get(args[0].toLowerCase());

                if (child == null) {
                    handleMessage(sender, MessageType.SUB_COMMAND_INVALID, label, args[0]);
                    return true;
                }

                if (child.isPlayerOnly() && !(sender instanceof Player)) {
                    handleMessage(sender, MessageType.PLAYER_ONLY);
                    return true;
                }

                if (child.getPermission() != null && !sender.hasPermission(child.getPermission())) {
                    handleMessage(sender, MessageType.NO_PERMISSION, child.getPermission());
                    return true;
                }

                if (args.length < child.length) {
                    handleMessage(sender, MessageType.SUB_COMMAND_USAGE, label, child.getUsage(), child.getDescription());
                    return true;
                }

                child.execute(sender, label, args);
                return true;
            }

            execute(sender, label, args);
            return true;
        });
        pluginCommand.setTabCompleter((sender, command, alias, args) -> {
            if (children != null && args.length > 1) {
                final AbstractCommand<P> child = children.get(args[0].toLowerCase());

                if (child != null) {
                    final List<String> result = child.onTabComplete(sender, command, alias, args);

                    if (result != null) {
                        return result;
                    }
                }
            }

            return onTabComplete(sender, command, alias, args);
        });
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 0) {
            return null;
        }

        if (args.length == 1 && children != null) {
            return children.values().stream()
                    .filter(child -> child.getName().startsWith(args[0].toLowerCase()))
                    .map(AbstractCommand::getName)
                    .distinct()
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());
        }

        return null;
    }

    protected boolean executeFirst(final CommandSender sender, final String label, final String[] args) {
        return false;
    }

    protected abstract void execute(final CommandSender sender, final String label, final String[] args);

    protected void handleMessage(final CommandSender sender, final MessageType type, final String... args) {
        sender.sendMessage(type.defaultMessage.format(args));
    }

    protected enum MessageType {

        PLAYER_ONLY("&cThis command can only be executed by a player!"),
        NO_PERMISSION("&cYou need the following permission: {0}"),
        SUB_COMMAND_INVALID("&c''{1}'' is not a valid sub command. Type /{0} for help."),
        SUB_COMMAND_USAGE("&cUsage: /{0} {1} - {2}");

        private final MessageFormat defaultMessage;

        MessageType(final String defaultMessage) {
            this.defaultMessage = new MessageFormat(StringUtil.color(defaultMessage));
        }
    }
}