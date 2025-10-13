package me.realized.duels.command.commands.duel;

import java.util.List;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.command.commands.duel.subcommands.AcceptCommand;
import me.realized.duels.command.commands.duel.subcommands.DenyCommand;
import me.realized.duels.command.commands.duel.subcommands.InventoryCommand;
import me.realized.duels.command.commands.duel.subcommands.StatsCommand;
import me.realized.duels.command.commands.duel.subcommands.ToggleCommand;
import me.realized.duels.command.commands.duel.subcommands.TopCommand;
import me.realized.duels.command.commands.duel.subcommands.VersionCommand;
import me.realized.duels.data.UserData;
import me.realized.duels.hook.hooks.CombatLogXHook;
import me.realized.duels.hook.hooks.CombatTagPlusHook;
import me.realized.duels.hook.hooks.PvPManagerHook;
import me.realized.duels.hook.hooks.VaultHook;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    private final CombatTagPlusHook combatTagPlus;
    private final PvPManagerHook pvpManager;
    private final CombatLogXHook combatLogX;
    private final WorldGuardHook worldGuard;
    private final VaultHook vault;

    public DuelCommand(final DuelsPlugin plugin) {
        super(plugin, "duel", Permissions.DUEL, true);
        child(
            new AcceptCommand(plugin),
            new DenyCommand(plugin),
            new StatsCommand(plugin),
            new ToggleCommand(plugin),
            new TopCommand(plugin),
            new InventoryCommand(plugin),
            new VersionCommand(plugin)
        );
        this.combatTagPlus = hookManager.getHook(CombatTagPlusHook.class);
        this.pvpManager = hookManager.getHook(PvPManagerHook.class);
        this.combatLogX = hookManager.getHook(CombatLogXHook.class);
        this.worldGuard = hookManager.getHook(WorldGuardHook.class);
        this.vault = hookManager.getHook(VaultHook.class);
    }

    @Override
    protected boolean executeFirst(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (userManager.get(player) == null) {
            lang.sendMessage(sender, "ERROR.data.load-failure");
            return true;
        }

        if (args.length == 0) {
            lang.sendMessage(sender, "COMMAND.duel.usage", "command", label);
            return true;
        }

        if (isChild(args[0])) {
            return false;
        }

        if (config.isRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(sender, "ERROR.duel.inventory-not-empty");
            return true;
        }

        if (config.isPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE) {
            lang.sendMessage(sender, "ERROR.duel.in-creative-mode");
            return true;
        }

        if (config.getBlacklistedWorlds().contains(player.getWorld().getName())) {
            lang.sendMessage(sender, "ERROR.duel.in-blacklisted-world");
            return true;
        }

        if ((combatTagPlus != null && combatTagPlus.isTagged(player))
            || (pvpManager != null && pvpManager.isTagged(player))
            || (combatLogX != null && combatLogX.isTagged(player))) {
            lang.sendMessage(sender, "ERROR.duel.is-tagged");
            return true;
        }

        String duelzone = null;

        if (worldGuard != null && config.isDuelzoneEnabled() && (duelzone = worldGuard.findDuelZone(player)) == null) {
            lang.sendMessage(sender, "ERROR.duel.not-in-duelzone", "regions", config.getDuelzones());
            return true;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.sender");
            return true;
        }

        if (spectateManager.isSpectating(player)) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.sender");
            return true;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null /*|| !player.canSee(target)*/) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return true;
        }

        if (player.equals(target)) {
            lang.sendMessage(sender, "ERROR.duel.is-self");
            return true;
        }

        final UserData user = userManager.get(target);

        if (user == null) {
            lang.sendMessage(sender, "ERROR.data.not-found", "name", target.getName());
            return true;
        }

        if (!sender.hasPermission(Permissions.ADMIN) && !user.canRequest()) {
            lang.sendMessage(sender, "ERROR.duel.requests-disabled", "name", target.getName());
            return true;
        }

        if (requestManager.has(player, target)) {
            lang.sendMessage(sender, "ERROR.duel.already-has-request", "name", target.getName());
            return true;
        }

        if (arenaManager.isInMatch(target)) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.target", "name", target.getName());
            return true;
        }

        if (spectateManager.isSpectating(target)) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.target", "name", target.getName());
            return true;
        }

        final Settings settings = settingManager.getSafely(player);
        // Reset bet to prevent accidents
        settings.setBet(0);
        settings.setTarget(target);
        settings.setBaseLoc(player);
        settings.setDuelzone(player, duelzone);

        boolean sendRequest = false;

        if (args.length > 1) {
            final int amount = NumberUtil.parseInt(args[1]).orElse(0);

            if (amount > 0 && config.isMoneyBettingEnabled()) {
                if (config.isMoneyBettingUsePermission() && !player.hasPermission(Permissions.MONEY_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
                    lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.MONEY_BETTING);
                    return true;
                }

                if (vault == null || vault.getEconomy() == null) {
                    lang.sendMessage(sender, "ERROR.setting.disabled-option", "option", lang.getMessage("GENERAL.betting"));
                    return true;
                }

                if (!vault.getEconomy().has(player, amount)) {
                    lang.sendMessage(sender, "ERROR.command.not-enough-money");
                    return true;
                }

                settings.setBet(amount);
            }

            if (args.length > 2) {
                if (args[2].equalsIgnoreCase("true")) {
                    if (!config.isItemBettingEnabled()) {
                        lang.sendMessage(player, "ERROR.setting.disabled-option", "option", lang.getMessage("GENERAL.item-betting"));
                        return true;
                    }

                    if (config.isItemBettingUsePermission() && !player.hasPermission(Permissions.ITEM_BETTING) && !player.hasPermission(Permissions.SETTING_ALL)) {
                        lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ITEM_BETTING);
                        return true;
                    }

                    settings.setItemBetting(true);
                }

                if (args.length > 3) {
                    if (args[3].equals("-")) {
                        if (!config.isOwnInventoryEnabled()) {
                            lang.sendMessage(player, "ERROR.setting.disabled-option", "option", lang.getMessage("GENERAL.own-inventory"));
                            return true;
                        }

                        if (config.isOwnInventoryUsePermission() && !player.hasPermission(Permissions.OWN_INVENTORY) && !player.hasPermission(Permissions.SETTING_ALL)) {
                            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.OWN_INVENTORY);
                            return true;
                        }

                        settings.setOwnInventory(true);
                    } else if (!config.isKitSelectingEnabled()) {
                        lang.sendMessage(player, "ERROR.setting.disabled-option", "option", lang.getMessage("GENERAL.kit-selector"));
                        return true;
                    } else {
                        final String name = StringUtil.join(args, " ", 3, args.length);
                        final KitImpl kit = kitManager.get(name);

                        if (kit == null) {
                            lang.sendMessage(sender, "ERROR.kit.not-found", "name", name);
                            return true;
                        }

                        final String permission = String.format(Permissions.KIT, name.replace(" ", "-").toLowerCase());

                        if (kit.isUsePermission() && !player.hasPermission(Permissions.KIT_ALL) && !player.hasPermission(permission)) {
                            lang.sendMessage(player, "ERROR.no-permission", "permission", permission);
                            return true;
                        }

                        settings.setKit(kit);
                    }

                    sendRequest = true;
                }
            }
        }

        if (sendRequest) {
            // If all settings were selected via command, send request without opening settings GUI.
            requestManager.send(player, target, settings);
        } else if (config.isOwnInventoryEnabled()) {
            // If own inventory is enabled, prompt request settings GUI.
            settings.openGui(player);
        } else {
            // Maintain old behavior: If own inventory is disabled, prompt kit selector first instead of request settings GUI.
            kitManager.getGui().open(player);
        }

        return true;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {}

    // Disables default TabCompleter
    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return null;
    }
}
