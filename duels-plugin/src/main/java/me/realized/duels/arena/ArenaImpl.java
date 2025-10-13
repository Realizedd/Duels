package me.realized.duels.arena;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.event.arena.ArenaSetPositionEvent;
import me.realized.duels.api.event.arena.ArenaStateChangeEvent;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.api.event.match.MatchEndEvent.Reason;
import me.realized.duels.gui.BaseButton;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.queue.Queue;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.function.Pair;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArenaImpl extends BaseButton implements Arena {

    @Getter
    private final String name;
    @Getter
    private boolean disabled;
    @Getter
    private final Set<KitImpl> kits = new HashSet<>();
    @Getter
    private final Map<Integer, Location> positions = new HashMap<>();
    @Getter
    private MatchImpl match;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private Countdown countdown;
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private boolean removed;

    public ArenaImpl(final DuelsPlugin plugin, final String name, final boolean disabled) {
        super(plugin, ItemBuilder
            .of(Items.EMPTY_MAP)
            .name(plugin.getLang().getMessage("GUI.arena-selector.buttons.arena.name", "name", name))
            .lore(plugin.getLang().getMessage("GUI.arena-selector.buttons.arena.lore-unavailable").split("\n"))
            .build()
        );
        this.name = name;
        this.disabled = disabled;
    }

    public ArenaImpl(final DuelsPlugin plugin, final String name) {
        this(plugin, name, false);
    }

    public void refreshGui(final boolean available) {
        setLore(lang.getMessage("GUI.arena-selector.buttons.arena.lore-" + (available ? "available" : "unavailable")).split("\n"));
        arenaManager.getGui().calculatePages();
    }

    @Nullable
    @Override
    public Location getPosition(final int pos) {
        return positions.get(pos);
    }

    @Override
    public boolean setPosition(@Nullable final Player source, final int pos, @NotNull final Location location) {
        Objects.requireNonNull(location, "location");

        if (pos <= 0 || pos > 2) {
            return false;
        }

        final ArenaSetPositionEvent event = new ArenaSetPositionEvent(source, this, pos, location);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        positions.put(pos, location);
        arenaManager.saveArenas();
        refreshGui(isAvailable());
        return true;
    }

    @Override
    public boolean setPosition(final int pos, @NotNull final Location location) {
        return setPosition(null, pos, location);
    }

    @Override
    public boolean setDisabled(@Nullable final CommandSender source, final boolean disabled) {
        final ArenaStateChangeEvent event = new ArenaStateChangeEvent(source, this, disabled);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        this.disabled = event.isDisabled();
        arenaManager.saveArenas();
        refreshGui(isAvailable());
        return true;
    }

    @Override
    public boolean setDisabled(final boolean disabled) {
        return setDisabled(null, disabled);
    }

    public boolean isBoundless() {
        return kits.isEmpty();
    }

    public boolean isBound(@Nullable final KitImpl kit) {
        return kit != null && kits.contains(kit);
    }

    public void bind(final KitImpl kit) {
        if (isBound(kit)) {
            kits.remove(kit);
        } else {
            kits.add(kit);
        }
        arenaManager.saveArenas();
    }

    @Override
    public boolean isUsed() {
        return match != null;
    }

    public boolean isAvailable() {
        return !isDisabled() && !isUsed() && getPosition(1) != null && getPosition(2) != null;
    }

    public MatchImpl startMatch(final KitImpl kit, final Map<UUID, List<ItemStack>> items, final int bet, final Queue source) {
        this.match = new MatchImpl(this, kit, items, bet, source);
        refreshGui(false);
        return match;
    }

    public void endMatch(final UUID winner, final UUID loser, final Reason reason) {
        spectateManager.stopSpectating(this);

        final MatchEndEvent event = new MatchEndEvent(match, winner, loser, reason);
        Bukkit.getPluginManager().callEvent(event);

        final Queue source = match.getSource();
        match.setFinished();

        for(Block block : match.placedBlocks) {
            block.setType(Material.AIR);
        }

        for(Map.Entry<Location, BlockData> map : match.brokenBlocks.entrySet()) {
            map.getKey().getBlock().setBlockData(map.getValue());
        }

        for (Block block : match.liquids) {
            Location loc = block.getLocation();
            int radius = 1;

            while (true) {
                boolean waterFound = false;

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block findBlock = loc.clone().add(x, y, z).getBlock();
                            String type = findBlock.getType().name().toLowerCase();

                            if (type.contains("water") || type.contains("lava") || type.contains("cobblestone") || type.contains("obsidian")) {
                                waterFound = true;
                                findBlock.setType(Material.AIR);
                            }
                        }
                    }
                }

                if (!waterFound) {
                    break;
                }

                radius++;
            }
        }

        if(config.isClearItemsAfterMatch()) {
            match.droppedItems.forEach(Entity::remove);
        }

        match = null;

        if (source != null) {
            source.update();
            queueManager.getGui().calculatePages();
        }

        refreshGui(true);
    }

    public void startCountdown(final String kit, final Map<UUID, Pair<String, Integer>> info) {
        final List<String> messages = config.getCdMessages();

        if (messages.isEmpty()) {
            return;
        }

        this.countdown = new Countdown(plugin, this, kit, info, messages, config.getTitles());
        countdown.runTaskTimer(plugin, 0L, 20L);
    }

    boolean isCounting() {
        return countdown != null;
    }

    @Override
    public boolean has(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return isUsed() && !match.getPlayerMap().getOrDefault(player, new MatchImpl.PlayerStatus(true)).isDead;
    }

    public void add(final Player player) {
        if (isUsed()) {
            match.getPlayerMap().put(player, new MatchImpl.PlayerStatus(false));
        }
    }

    public void remove(final Player player) {
        if (isUsed() && match.getPlayerMap().containsKey(player)) {
            match.getPlayerMap().put(player, new MatchImpl.PlayerStatus(true));
        }
    }

    public boolean isEndGame() {
        return size() <= 1;
    }

    public int size() {
        return isUsed() ? match.getAlivePlayers().size() : 0;
    }

    public Player first() {
        return isUsed() ? match.getAlivePlayers().iterator().next() : null;
    }

    public Player getOpponent(final Player player) {
        return isUsed() ? match.getAllPlayers().stream().filter(other -> !player.equals(other)).findFirst().orElse(null) : null;
    }

    public Set<Player> getPlayers() {
        return isUsed() ? match.getAllPlayers() : Collections.emptySet();
    }

    public void broadcast(final String message) {
        final List<Player> receivers = Lists.newArrayList(getPlayers());
        spectateManager.getSpectatorsImpl(this)
            .stream()
            .map(spectator -> Bukkit.getPlayer(spectator.getUuid()))
            .forEach(receivers::add);
        receivers.forEach(player -> player.sendMessage(message));
    }

    @Override
    public void onClick(final Player player) {
        if (!isAvailable()) {
            return;
        }

        final Settings settings = settingManager.getSafely(player);
        final String kitName = settings.getKit() != null ? settings.getKit().getName() : lang.getMessage("GENERAL.none");

        if (!arenaManager.isSelectable(settings.getKit(), this)) {
            lang.sendMessage(player, "ERROR.setting.arena-not-applicable", "kit", kitName, "arena", name);
            return;
        }

        settings.setArena(this);
        settings.openGui(player);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final ArenaImpl arena = (ArenaImpl) other;
        return Objects.equals(name, arena.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
