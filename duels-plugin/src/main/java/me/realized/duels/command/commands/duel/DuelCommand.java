package me.realized.duels.command.commands.duel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.alessiodp.parties.api.interfaces.PartyPlayer;
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
import me.realized.duels.hook.hooks.PartiesHook;
import me.realized.duels.hook.hooks.VaultHook;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.kit.KitImpl;
import com.alessiodp.parties.api.interfaces.Party;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.function.Pair;
import me.realized.duels.util.validator.ValidatorUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand extends BaseCommand {

    private final WorldGuardHook worldGuard;
    private final VaultHook vault;
    private final PartiesHook parties;

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
        this.worldGuard = hookManager.getHook(WorldGuardHook.class);
        this.vault = hookManager.getHook(VaultHook.class);
        this.parties = hookManager.getHook(PartiesHook.class);
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
        
        final com.alessiodp.parties.api.interfaces.Party party = parties.getApi().getPartyOfPlayer(player.getUniqueId());
        final Collection<Player> players = party == null ? Collections.singleton(player) : partyManager.getOnlinePlayers(party);
        
        if (!ValidatorUtil.validate(validatorManager.getDuelSelfValidators(), player, party, players)) {
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        final com.alessiodp.parties.api.interfaces.Party targetParty = target == null ? parties.getApi().getParty(args[0]) : parties.getApi().getPartyOfPlayer(target.getUniqueId());

        if (target == null && targetParty != null && targetParty.getLeader() != null) {
            target = Bukkit.getPlayer(targetParty.getLeader());
        }

        if (target == null || !player.canSee(target)) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[0]);
            return true;
        }

        final Collection<Player> targetPlayers = targetParty == null ? Collections.singleton(target) : partyManager.getOnlinePlayers(targetParty);

        if (!ValidatorUtil.validate(validatorManager.getDuelTargetValidators(), new Pair<>(player, target), targetParty, targetPlayers)) {
            return true;
        }

        final Settings settings = settingManager.getSafely(player);
        // Reset bet to prevent accidents
        settings.setBet(0);
        settings.setTarget(target);
        settings.setSenderParty(party);
        settings.setTargetParty(targetParty);
        settings.clearCache();
        players.forEach(all -> {
            settings.setBaseLoc(all);
            settings.setDuelzone(all, worldGuard != null ? worldGuard.findDuelZone(all) : null);
        });

        boolean sendRequest = false;

        if (args.length > 1) {
            if (party != null) {
                lang.sendMessage(sender, "ERROR.party-duel.option-unavailable");
                return true;
            }

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
