package me.realized.duels.duel;

import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.arena.ArenaImpl;
import me.realized.duels.arena.MatchImpl;
import me.realized.duels.data.MatchData;
import me.realized.duels.data.UserData;
import me.realized.duels.hook.hooks.*;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.player.PlayerInfo;
import me.realized.duels.util.*;
import me.realized.duels.util.compat.Titles;
import me.realized.duels.util.inventory.InventoryUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DuelManagerHandler implements Loadable {
    private final DuelManager duelManager;

    public DuelManagerHandler(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    @Override
    public void handleLoad() {
        duelManager.setQueueManager(duelManager.getPlugin().getQueueManager());
        duelManager.setTeleport(duelManager.getPlugin().getTeleport());
        duelManager.setCombatTagPlus(duelManager.getPlugin().getHookManager().getHook(CombatTagPlusHook.class));
        duelManager.setPvpManager(duelManager.getPlugin().getHookManager().getHook(PvPManagerHook.class));
        duelManager.setCombatLogX(duelManager.getPlugin().getHookManager().getHook(CombatLogXHook.class));
        duelManager.setVault(duelManager.getPlugin().getHookManager().getHook(VaultHook.class));
        duelManager.setEssentials(duelManager.getPlugin().getHookManager().getHook(EssentialsHook.class));
        duelManager.setMcMMO(duelManager.getPlugin().getHookManager().getHook(McMMOHook.class));
        duelManager.setWorldGuard(duelManager.getPlugin().getHookManager().getHook(WorldGuardHook.class));
        duelManager.setMyPet(duelManager.getPlugin().getHookManager().getHook(MyPetHook.class));

        if (duelManager.getConfig().getMaxDuration() > 0) {
            duelManager.setDurationCheckTask(duelManager.getPlugin().doSyncRepeat(() -> {
                for (final ArenaImpl arena : duelManager.getArenaManager().getArenasImpl()) {
                    final MatchImpl match = arena.getMatch();

                    // Only handle undecided matches (size > 1)
                    if (match == null || match.getDurationInMillis() < (duelManager.getConfig().getMaxDuration() * 60 * 1000L) || arena.size() <= 1) {
                        continue;
                    }

                    for (final Player player : match.getAllPlayers()) {
                        handleTie(player, arena, match, true);
                        duelManager.getLang().sendMessage(player, "DUEL.on-end.tie");
                    }

                    arena.endMatch(null, null, MatchEndEvent.Reason.MAX_TIME_REACHED);
                }
            }, 0L, 20L).getTaskId());
        }
    }

    @Override
    public void handleUnload() {
        duelManager.getPlugin().cancelTask(duelManager.getDurationCheckTask());

        /*
        3 Cases:
        1. size = 2: Match outcome is yet to be decided (INGAME phase)
        2. size = 1: Match ended with a winner and is in ENDGAME phase
        3. size = 0: Match ended in a tie (or winner killed themselves during ENDGAME phase) and is in ENDGAME phase
        */
        for (final ArenaImpl arena : duelManager.getArenaManager().getArenasImpl()) {
            final MatchImpl match = arena.getMatch();

            if (match == null) {
                continue;
            }

            final int size = arena.size();
            final boolean winnerDecided = size == 1;

            if (winnerDecided) {
                final Player winner = arena.first();
                duelManager.getLang().sendMessage(winner, "DUEL.on-end.plugin-disable");
                handleWin(winner, arena.getOpponent(winner), arena, match);
            } else {
                final boolean ongoing = size > 1;

                for (final Player player : match.getAllPlayers()) {
                    duelManager.getLang().sendMessage(player, "DUEL.on-end.plugin-disable");
                    handleTie(player, arena, match, ongoing);
                }
            }

            arena.endMatch(null, null, MatchEndEvent.Reason.PLUGIN_DISABLE);
        }
    }

    /**
     * Resets the player's inventory and balance in the case of a tie game.
     *
     * @param player Player to reset state
     * @param arena  Arena the match is taking place
     * @param match  Match the player is in
     * @param alive  Whether the player was alive in the match when the method was called.
     */
    void handleTie(final Player player, final ArenaImpl arena, final MatchImpl match, boolean alive) {
        arena.remove(player);

        // Reset player balance if there was a bet placed.
        if (duelManager.getVault() != null && match.getBet() > 0) {
            duelManager.getVault().add(match.getBet(), player);
        }

        if (duelManager.getMcMMO() != null) {
            duelManager.getMcMMO().enableSkills(player);
        }

        final PlayerInfo info = duelManager.getPlayerManager().get(player);
        final List<ItemStack> items = match.getItems(player);

        if (alive) {
            PlayerUtil.reset(player);
            duelManager.getPlayerManager().remove(player);

            if (info != null) {
                duelManager.getTeleport().tryTeleport(player, info.getLocation());
                info.restore(player);
            } else {
                // If somehow PlayerInfo is not found...
                duelManager.getTeleport().tryTeleport(player, duelManager.getPlayerManager().getLobby());
            }

            // Give back bet items
            InventoryUtil.addOrDrop(player, items);
        } else if (info != null) {
            // If player remained dead during ENDGAME phase, add the items to cached PlayerInfo of the player.
            info.getExtra().addAll(items);
        } else {
            InventoryUtil.addOrDrop(player, items);
        }
    }

    /**
     * Rewards the duel winner with money and items bet on the match.
     *
     * @param player   Player determined to be the winner
     * @param opponent Player that opposed the winner
     * @param arena    Arena the match is taking place
     * @param match    Match the player is in
     */
    void handleWin(final Player player, final Player opponent, final ArenaImpl arena, final MatchImpl match) {
        arena.remove(player);

        final String opponentName = opponent != null ? opponent.getName() : duelManager.getLang().getMessage("GENERAL.none");

        if (duelManager.getVault() != null && match.getBet() > 0) {
            final int amount = match.getBet() * 2;
            duelManager.getVault().add(amount, player);
            duelManager.getLang().sendMessage(player, "DUEL.reward.money.message", "name", opponentName, "money", amount);

            final String title = duelManager.getLang().getMessage("DUEL.reward.money.title", "name", opponentName, "money", amount);

            if (title != null) {
                Titles.send(player, title, null, 0, 20, 50);
            }
        }

        if (duelManager.getMcMMO() != null) {
            duelManager.getMcMMO().enableSkills(player);
        }

        final PlayerInfo info = duelManager.getPlayerManager().get(player);
        final List<ItemStack> items = match.getItems();

        if (!player.isDead()) {
            duelManager.getPlayerManager().remove(player);
            PlayerUtil.reset(player);

            if (info != null) {
                duelManager.getTeleport().tryTeleport(player, info.getLocation());
                info.restore(player);
            }

            if (InventoryUtil.addOrDrop(player, items)) {
                duelManager.getLang().sendMessage(player, "DUEL.reward.items.message", "name", opponentName);
            }
        } else {
            info.getExtra().addAll(items);
        }
    }

    void handleInventories(final MatchImpl match) {
        if (!duelManager.getConfig().isDisplayInventories()) {
            return;
        }

        String color = duelManager.getLang().getMessage("DUEL.inventories.name-color");
        final TextBuilder builder = TextBuilder.of(duelManager.getLang().getMessage("DUEL.inventories.message"));
        final Set<Player> players = match.getAllPlayers();
        final Iterator<Player> iterator = players.iterator();

        while (iterator.hasNext()) {
            final Player player = iterator.next();
            builder.add(StringUtil.color(color + player.getName()), ClickEvent.Action.RUN_COMMAND, "/duel _ " + player.getUniqueId());

            if (iterator.hasNext()) {
                builder.add(StringUtil.color(color + ", "));
            }
        }

        builder.send(players);
    }

    void handleStats(final MatchImpl match, final UserData winner, final UserData loser, final MatchData matchData) {
        if (winner != null && loser != null) {
            winner.addWin();
            loser.addLoss();
            winner.addMatch(matchData);
            loser.addMatch(matchData);

            final KitImpl kit = match.getKit();
            int winnerRating = kit == null ? winner.getRating() : winner.getRating(kit);
            int loserRating = kit == null ? loser.getRating() : loser.getRating(kit);
            int change = 0;

            if (duelManager.getConfig().isRatingEnabled() && !(!match.isFromQueue() && duelManager.getConfig().isRatingQueueOnly())) {
                change = NumberUtil.getChange(duelManager.getConfig().getKFactor(), winnerRating, loserRating);
                winner.setRating(kit, winnerRating = winnerRating + change);
                loser.setRating(kit, loserRating = loserRating - change);
            }

            final String message = duelManager.getLang().getMessage("DUEL.on-end.opponent-defeat",
                    "winner", winner.getName(),
                    "loser", loser.getName(),
                    "health", matchData.getHealth(),
                    "kit", matchData.getKit(),
                    "arena", match.getArena().getName(),
                    "winner_rating", winnerRating,
                    "loser_rating", loserRating,
                    "change", change
            );

            if (message == null) {
                return;
            }

            if (duelManager.getConfig().isArenaOnlyEndMessage()) {
                match.getArena().broadcast(message);
            } else {
                Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
            }
        }
    }
}