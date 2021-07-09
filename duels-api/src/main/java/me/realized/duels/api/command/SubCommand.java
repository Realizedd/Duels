package me.realized.duels.api.command;

import me.realized.duels.api.Duels;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An abstract SubCommand class that hooks into commands registered by Duels.
 *
 * @see Duels#registerSubCommand(String, SubCommand)
 */
public abstract class SubCommand {

    private final String name, usage, description, permission;
    private final boolean playerOnly;
    private final int length;
    private final String[] aliases;

    /**
     * The constructor for a subcommand.
     *
     * @param name        Name of this subcommand. Should not be null!
     * @param usage       Usage of this subcommand. May be null.
     * @param description Description of this subcommand. May be null.
     * @param permission  Permission of this subcommand. May be null.
     * @param playerOnly  Whether or not this subcommand should be player only.
     * @param length      Length of this subcommand. Must be greater than or equal to 1.
     * @param aliases     Aliases of this subcommand.
     */
    public SubCommand(@Nonnull final String name, @Nullable final String usage, @Nullable final String description, @Nullable final String permission,
                      final boolean playerOnly, final int length, final String... aliases) {
        Objects.requireNonNull(name, "name");
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.length = Math.max(length, 1);
        this.aliases = aliases;
    }

    /**
     * The name of this subcommand.
     *
     * @return Never-null {@link String} that is the name of this subcommand.
     */
    public String getName() {
        return name;
    }

    /**
     * The usage of this subcommand.
     *
     * @return Usage of this subcommand or null if unspecified.
     */
    public String getUsage() {
        return usage;
    }

    /**
     * The description of this subcommand.
     *
     * @return Description of this subcommand or null if unspecified.
     */
    public String getDescription() {
        return description;
    }

    /**
     * The permission of this subcommand. If set to null, it will inherit its parent's permission.
     *
     * @return Permission of this subcommand or null if unspecified.
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Whether or not this subcommand can only be used by a player. If the parent of this subcommand is player only, this value will not be considered.
     *
     * @return True if this subcommand is player only. False otherwise.
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }

    /**
     * The length of this subcommand.
     *
     * @return Length of this subcommand.
     */
    public int getLength() {
        return length;
    }

    /**
     * The aliases of this subcommand.
     *
     * @return Aliases of this subcommand.
     */
    public String[] getAliases() {
        return aliases;
    }

    public abstract void execute(final CommandSender sender, final String label, final String[] args);
}
