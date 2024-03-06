package me.realized.duels.arena;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.Getter;
import me.realized.duels.api.match.Match;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.queue.Queue;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MatchImpl implements Match {

    public static class PlayerStatus {

        public PlayerStatus(boolean isDead) {
            this.isDead = isDead;
        }

        // Player is dead value
        public boolean isDead;
        // How much damage to your opponent.
        public double damageCount;

        public int hits;

    }

    @Getter
    private final ArenaImpl arena;
    @Getter
    private final long start;
    @Getter
    private final KitImpl kit;
    private final Map<UUID, List<ItemStack>> items;
    @Getter
    private final int bet;
    @Getter
    private final Queue source;

    @Getter
    private boolean finished;

    @Getter
    public List<Item> droppedItems = new ArrayList<>();

    // Default value for players is false, which is set to true if player is killed in the match.
    private final Map<Player, PlayerStatus> players = new HashMap<>();

    MatchImpl(final ArenaImpl arena, final KitImpl kit, final Map<UUID, List<ItemStack>> items, final int bet, final Queue source) {
        this.arena = arena;
        this.start = System.currentTimeMillis();
        this.kit = kit;
        this.items = items;
        this.bet = bet;
        this.source = source;
    }

    Map<Player, PlayerStatus> getPlayerMap() {
        return players;
    }

    Set<Player> getAlivePlayers() {
        return players.entrySet().stream().filter(entry -> !entry.getValue().isDead).map(Entry::getKey).collect(Collectors.toSet());
    }

    public void addDamageToPlayer(Player player, double damage) {
        PlayerStatus status = players.get(player);
        status.damageCount += damage;
        status.hits++;
    }

    public int getHits(Player player) {
        return players.get(player).hits;
    }

    public Player getWinnerOfDamage() {
        Player winner = players.entrySet()
                .stream()
                .max(Comparator.comparingDouble(entry -> entry.getValue().damageCount))
                .map(Map.Entry::getKey)
                .orElse(null);

        return winner;
    }

    public Player getLooserOfDamage() {
        Player looser = players.entrySet()
                .stream()
                .min(Comparator.comparingDouble(entry -> entry.getValue().damageCount))
                .map(Map.Entry::getKey)
                .orElse(null);

        return looser;
    }

    public Set<Player> getAllPlayers() {
        return players.keySet();
    }

    public boolean isDead(final Player player) {
        return players.getOrDefault(player, new PlayerStatus(true)).isDead;
    }

    public boolean isFromQueue() {
        return source != null;
    }

    public boolean isOwnInventory() {
        return kit == null;
    }
    
    public List<ItemStack> getItems() {
        return items != null ? items.values().stream().flatMap(Collection::stream).collect(Collectors.toList()) : Collections.emptyList();
    }

    void setFinished() {
        finished = true;
    }

    public long getDurationInMillis() {
        return System.currentTimeMillis() - start;
    }

    @NotNull
    @Override
    public List<ItemStack> getItems(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");

        if (this.items == null) {
            return Collections.emptyList();
        }

        final List<ItemStack> items = this.items.get(player.getUniqueId());
        return items != null ? items : Collections.emptyList();
    }

    @NotNull
    @Override
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(getAlivePlayers());
    }

    @NotNull
    @Override
    public Set<Player> getStartingPlayers() {
        return Collections.unmodifiableSet(getAllPlayers());
    }
}
