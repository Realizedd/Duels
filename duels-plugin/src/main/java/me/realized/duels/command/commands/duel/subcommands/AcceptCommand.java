package me.realized.duels.command.commands.duel.subcommands;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.request.RequestAcceptEvent;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.hook.hooks.AntiRelogHook;
import me.realized.duels.hook.hooks.CombatLogXHook;
import me.realized.duels.hook.hooks.CombatTagPlusHook;
import me.realized.duels.hook.hooks.PvPManagerHook;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.request.RequestImpl;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptCommand extends BaseCommand {

    private final CombatTagPlusHook combatTagPlus;
    private final PvPManagerHook pvpManager;
    private final CombatLogXHook combatLogX;
    private final WorldGuardHook worldGuard;
    private final AntiRelogHook antiRelog;

    public AcceptCommand(final DuelsPlugin plugin) {
        super(plugin, "accept", "accept [player]", "Accepts a duel request.", 2, true);
        this.combatTagPlus = hookManager.getHook(CombatTagPlusHook.class);
        this.pvpManager = hookManager.getHook(PvPManagerHook.class);
        this.combatLogX = plugin.getHookManager().getHook(CombatLogXHook.class);
        this.worldGuard = hookManager.getHook(WorldGuardHook.class);
        this.antiRelog = hookManager.getHook(AntiRelogHook.class);
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (config.isRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(sender, "ERROR.duel.inventory-not-empty");
            return;
        }

        if (config.isPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE) {
            lang.sendMessage(sender, "ERROR.duel.in-creative-mode");
            return;
        }

        if ( (combatTagPlus != null && combatTagPlus.isTagged(player))
            || (pvpManager != null && pvpManager.isTagged(player))
            || (combatLogX != null && combatLogX.isTagged(player))
            || (antiRelog != null && antiRelog.isTagged(player))
        ) {
            lang.sendMessage(sender, "ERROR.duel.is-tagged");
            return;
        }

        String duelzone = null;

        if (worldGuard != null && config.isDuelzoneEnabled() && (duelzone = worldGuard.findDuelZone(player)) == null) {
            lang.sendMessage(sender, "ERROR.duel.not-in-duelzone", "regions", config.getDuelzones());
            return;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.sender");
            return;
        }

        if (spectateManager.isSpectating(player)) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.sender");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null /*|| !player.canSee(target)*/) {
            lang.sendMessage(sender, "ERROR.player.not-found", "name", args[1]);
            return;
        }

        final RequestImpl request = requestManager.get(target, player);

        if (request == null) {
            lang.sendMessage(sender, "ERROR.duel.no-request", "name", target.getName());
            return;
        }

        final RequestAcceptEvent event = new RequestAcceptEvent(player, target, request);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        requestManager.remove(target, player);

        if (arenaManager.isInMatch(target)) {
            lang.sendMessage(sender, "ERROR.duel.already-in-match.target", "name", target.getName());
            return;
        }

        if (spectateManager.isSpectating(target)) {
            lang.sendMessage(sender, "ERROR.spectate.already-spectating.target", "name", target.getName());
            return;
        }

        final Settings settings = request.getSettings();
        final String kit = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.not-selected");
        final String arena = settings.getArena() != null ? settings.getArena().getName() : lang.getMessage("GENERAL.random");
        final double bet = settings.getBet();
        final String itemBetting = settings.isItemBetting() ? lang.getMessage("GENERAL.enabled") : lang.getMessage("GENERAL.disabled");
        lang.sendMessage(player, "COMMAND.duel.request.accept.receiver",
            "name", target.getName(), "kit", kit, "arena", arena, "bet_amount", bet, "item_betting", itemBetting);
        lang.sendMessage(target, "COMMAND.duel.request.accept.sender",
            "name", player.getName(), "kit", kit, "arena", arena, "bet_amount", bet, "item_betting", itemBetting);

        if (settings.isItemBetting()) {
            settings.setBaseLoc(player);
            settings.setDuelzone(player, duelzone);
            bettingManager.open(settings, target, player);
        } else {
            duelManager.startMatch(player, target, settings, null, null);
        }
    }
}
