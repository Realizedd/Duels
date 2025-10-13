package me.realized.duels.command.commands.bot;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.command.BaseCommand;
import me.realized.duels.hook.hooks.CombatLogXHook;
import me.realized.duels.hook.hooks.CombatTagPlusHook;
import me.realized.duels.hook.hooks.PvPManagerHook;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BotDuelCommand extends BaseCommand {

    private final CombatTagPlusHook combatTagPlus;
    private final PvPManagerHook pvpManager;
    private final CombatLogXHook combatLogX;
    private final WorldGuardHook worldGuard;

    public BotDuelCommand(DuelsPlugin plugin) {
        super(plugin, "botduel", "/botduel", "Duel with bots", Permissions.DUEL_BOT, 0, true,
                "botfight", "pvpbot", "bot", "duelbot");

        this.combatTagPlus = hookManager.getHook(CombatTagPlusHook.class);
        this.pvpManager = hookManager.getHook(PvPManagerHook.class);
        this.combatLogX = hookManager.getHook(CombatLogXHook.class);
        this.worldGuard = hookManager.getHook(WorldGuardHook.class);
    }

    @Override
    protected boolean executeFirst(final CommandSender sender, final String label, final String[] args) {
        final Player player = (Player) sender;

        if (userManager.get(player) == null) {
            lang.sendMessage(sender, "ERROR.data.load-failure");
            return true;
        }

        if (args.length != 0) {
            lang.sendMessage(sender, "COMMAND.duel.usage", "command", label);
            return true;
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

        final Settings settings = settingManager.getSafely(player, true);

        settings.setTarget(player);
        settings.setBaseLoc(player);
        settings.setDuelzone(player, duelzone);

        // Maintain old behavior: If own inventory is disabled, prompt kit selector first instead of request settings GUI.
        kitManager.getGui().open(player);

        return true;
    }

    @Override
    protected void execute(final CommandSender sender, final String label, final String[] args) {}

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 0) {
            return Arrays.asList("botduel");
        }

        return null;
    }
}
