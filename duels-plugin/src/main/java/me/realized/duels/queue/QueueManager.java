package me.realized.duels.queue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.queue.QueueCreateEvent;
import me.realized.duels.api.event.queue.QueueJoinEvent;
import me.realized.duels.api.event.queue.QueueLeaveEvent;
import me.realized.duels.api.event.queue.QueueRemoveEvent;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.queue.DQueue;
import me.realized.duels.api.queue.DQueueManager;
import me.realized.duels.arena.ArenaManagerImpl;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.QueueData;
import me.realized.duels.data.UserData;
import me.realized.duels.data.UserManagerImpl;
import me.realized.duels.duel.DuelManager;
import me.realized.duels.hook.hooks.CombatTagPlusHook;
import me.realized.duels.hook.hooks.PvPManagerHook;
import me.realized.duels.hook.hooks.VaultHook;
import me.realized.duels.hook.hooks.worldguard.WorldGuardHook;
import me.realized.duels.kit.KitManagerImpl;
import me.realized.duels.setting.Settings;
import me.realized.duels.spectate.SpectateManagerImpl;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.MultiPageGui;
import me.realized.duels.util.inventory.InventoryUtil;
import me.realized.duels.util.inventory.ItemBuilder;
import me.realized.duels.util.io.FileUtil;
import me.realized.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueueManager implements Loadable, DQueueManager, Listener {

    private static final String FILE_NAME = "queues.json";

    private static final String QUEUES_LOADED = "Loaded %s queue(s).";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final UserManagerImpl userManager;
    private final KitManagerImpl kitManager;
    private final ArenaManagerImpl arenaManager;
    private final SpectateManagerImpl spectateManager;
    private final DuelManager duelManager;
    private final File file;

    private final List<Queue> queues = new ArrayList<>();

    private CombatTagPlusHook combatTagPlus;
    private PvPManagerHook pvpManager;
    private WorldGuardHook worldGuard;
    private VaultHook vault;
    private int queueTask;

    @Getter
    private MultiPageGui<DuelsPlugin> gui;

    public QueueManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.userManager = plugin.getUserManager();
        this.kitManager = plugin.getKitManager();
        this.arenaManager = plugin.getArenaManager();
        this.spectateManager = plugin.getSpectateManager();
        this.duelManager = plugin.getDuelManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private boolean canFight(final Kit kit, final UserData first, final UserData second) {
        if (kit == null || !config.isRatingEnabled()) {
            return true;
        }

        if (first != null && second != null) {
            final int firstRating = first.getRating(kit);
            final int secondRating = second.getRating(kit);
            final int kFactor = config.getKFactor();
            return NumberUtil.getChange(kFactor, firstRating, secondRating) != 0 && NumberUtil.getChange(kFactor, secondRating, firstRating) != 0;
        }

        return false;
    }

    @Override
    public void handleLoad() throws IOException {
        this.gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.queues.title"), config.getQueuesRows(), queues);
        gui.setSpaceFiller(Items.from(config.getQueuesFillerType(), config.getQueuesFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.queues.buttons.previous-page.name")).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.queues.buttons.next-page.name")).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.queues.buttons.empty.name")).build());
        plugin.getGuiListener().addGui(gui);

        if (FileUtil.checkNonEmpty(file, true)) {
            try (final Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
                final List<QueueData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<List<QueueData>>() {});

                if (data != null) {
                    data.forEach(queueData -> {
                        final Queue queue = queueData.toQueue(plugin);

                        if (queue != null && !queues.contains(queue)) {
                            queues.add(queue);
                        }
                    });
                }
            }
        }

        Log.info(this, String.format(QUEUES_LOADED, queues.size()));
        gui.calculatePages();

        this.combatTagPlus = plugin.getHookManager().getHook(CombatTagPlusHook.class);
        this.pvpManager = plugin.getHookManager().getHook(PvPManagerHook.class);
        this.worldGuard = plugin.getHookManager().getHook(WorldGuardHook.class);
        this.vault = plugin.getHookManager().getHook(VaultHook.class);
        this.queueTask = plugin.doSyncRepeat(() -> {
            boolean update = false;

            for (final Queue queue : queues) {
                final Set<QueueEntry> remove = new HashSet<>();

                for (final QueueEntry current : queue.getPlayers()) {
                    // player is already in a match
                    if (remove.contains(current)) {
                        continue;
                    }

                    final Player player = current.getPlayer();

                    for (final QueueEntry opponent : queue.getPlayers()) {
                        final Player other = opponent.getPlayer();

                        // opponent is already in a match or the rating difference is too high
                        if (current.equals(opponent) || remove.contains(opponent) || !canFight(queue.getKit(), userManager.get(player), userManager.get(other))) {
                            continue;
                        }

                        remove.add(current);
                        remove.add(opponent);

                        final Settings setting = new Settings(plugin);

                        if (queue.getKit() != null) {
                            setting.setKit(kitManager.get(queue.getKit().getName()));
                        } else {
                            setting.setOwnInventory(true);
                        }

                        setting.setBet(queue.getBet());
                        setting.getCache().put(player.getUniqueId(), current.getInfo());
                        setting.getCache().put(other.getUniqueId(), opponent.getInfo());

                        final String kit = queue.getKit() != null ? queue.getKit().getName() : lang.getMessage("GENERAL.none");
                        lang.sendMessage(player, "QUEUE.found-opponent", "name", other.getName(), "kit", kit, "bet_amount", queue.getBet());
                        lang.sendMessage(other, "QUEUE.found-opponent", "name", player.getName(), "kit", kit, "bet_amount", queue.getBet());
                        duelManager.startMatch(player, other, setting, null, queue);
                        break;
                    }
                }

                if (queue.removeAll(remove) && !update) {
                    update = true;
                }
            }

            if (update) {
                gui.calculatePages();
            }
        }, 20L, 40L).getTaskId();
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(queueTask);

        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        queues.clear();
    }

    private void saveQueues() {
        final List<QueueData> data = new ArrayList<>();

        for (final Queue queue : queues) {
            data.add(new QueueData(queue));
        }

        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
            JsonUtil.getObjectWriter().writeValue(writer, data);
            writer.flush();
        } catch (IOException ex) {
            Log.error(this, ex.getMessage(), ex);
        }
    }

    @Nullable
    @Override
    public Queue get(@Nullable final Kit kit, final int bet) {
        return queues.stream().filter(queue -> Objects.equals(kit, queue.getKit()) && queue.getBet() == bet).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public Queue get(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return queues.stream().filter(queue -> queue.isInQueue(player)).findFirst().orElse(null);
    }

    @Nullable
    public Queue randomQueue() {
        return !queues.isEmpty() ? queues.get(ThreadLocalRandom.current().nextInt(queues.size())) : null;
    }

    @Nullable
    @Override
    public Queue create(@Nullable final CommandSender source, @Nullable final Kit kit, final int bet) {
        final Queue queue = new Queue(plugin, kit, bet);

        if (queues.contains(queue)) {
            return null;
        }

        queues.add(queue);
        saveQueues();

        final QueueCreateEvent event = new QueueCreateEvent(source, queue);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return queue;
    }

    @Nullable
    @Override
    public Queue create(@Nullable final Kit kit, final int bet) {
        return create(null, kit, bet);
    }

    @Nullable
    @Override
    public Queue remove(@Nullable final CommandSender source, @Nullable final Kit kit, final int bet) {
        return remove(source, get(kit, bet));
    }

    @Nullable
    @Override
    public Queue remove(@Nullable final Kit kit, final int bet) {
        return remove(null, kit, bet);
    }

    @NotNull
    @Override
    public List<DQueue> getQueues() {
        return Collections.unmodifiableList(queues);
    }

    @Override
    public boolean isInQueue(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return queues.stream().anyMatch(queue -> queue.isInQueue(player));
    }

    @Override
    public boolean addToQueue(@NotNull final Player player, @NotNull final DQueue queue) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(queue, "queue");
        return queue(player, (Queue) queue);
    }

    @Nullable
    @Override
    public DQueue removeFromQueue(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return remove(player);
    }

    public Queue remove(final CommandSender source, final Queue queue) {
        if (queue == null || !queues.remove(queue)) {
            return null;
        }

        saveQueues();
        queue.getPlayers().forEach(entry -> lang.sendMessage(entry.getPlayer(), "QUEUE.remove"));
        queue.getPlayers().clear();
        queue.setRemoved(true);

        final QueueRemoveEvent event = new QueueRemoveEvent(source, queue);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return queue;
    }

    public boolean queue(final Player player, final Queue queue) {
        final Queue found = get(player);

        if (found != null) {
            if (found.equals(queue)) {
                queue.removePlayer(player);
                lang.sendMessage(player, "QUEUE.remove");
                return false;
            }

            lang.sendMessage(player, "ERROR.queue.already-in");
            return false;
        }

        if (spectateManager.isSpectating(player)) {
            lang.sendMessage(player, "ERROR.spectate.already-spectating.sender");
            return false;
        }

        if (arenaManager.isInMatch(player)) {
            lang.sendMessage(player, "ERROR.duel.already-in-match.sender");
            return false;
        }

        if (config.isRequiresClearedInventory() && InventoryUtil.hasItem(player)) {
            lang.sendMessage(player, "ERROR.duel.inventory-not-empty");
            return false;
        }

        if (config.isRequiresNoElytra() && InventoryUtil.wearingElytra(player)) {
            lang.sendMessage(player, "ERROR.duel.wearing-elytra");
            return false;
        }

        if (config.isPreventCreativeMode() && player.getGameMode() == GameMode.CREATIVE) {
            lang.sendMessage(player, "ERROR.duel.in-creative-mode");
            return false;
        }

        if ((combatTagPlus != null && combatTagPlus.isTagged(player)) || (pvpManager != null && pvpManager.isTagged(player))) {
            lang.sendMessage(player, "ERROR.duel.is-tagged");
            return false;
        }

        String duelzone = null;

        if (worldGuard != null && config.isDuelzoneEnabled() && (duelzone = worldGuard.findDuelZone(player)) == null) {
            lang.sendMessage(player, "ERROR.duel.not-in-duelzone", "regions", config.getDuelzones());
            return false;
        }

        if (queue.getBet() > 0 && vault != null && !vault.has(queue.getBet(), player)) {
            lang.sendMessage(player, "ERROR.queue.not-enough-money", "bet_amount", queue.getBet());
            return false;
        }

        final QueueJoinEvent event = new QueueJoinEvent(player, queue);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        queue.addPlayer(new QueueEntry(player, player.getLocation().clone(), duelzone));

        final String kit = queue.getKit() != null ? queue.getKit().getName() : lang.getMessage("GENERAL.none");
        lang.sendMessage(player, "QUEUE.add", "kit", kit, "bet_amount", queue.getBet());
        return true;
    }

    public Queue remove(final Player player) {
        for (final Queue queue : queues) {
            if (queue.removePlayer(player)) {
                final QueueLeaveEvent event = new QueueLeaveEvent(player, queue);
                Bukkit.getPluginManager().callEvent(event);
                lang.sendMessage(player, "QUEUE.remove");
                return queue;
            }
        }

        return null;
    }

    @EventHandler
    public void on(final PlayerQuitEvent event) {
        remove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().substring(1).split(" ")[0].toLowerCase();

        if (!isInQueue(event.getPlayer()) || !config.getQueueBlacklistedCommands().contains(command)) {
            return;
        }

        event.setCancelled(true);
        lang.sendMessage(event.getPlayer(), "QUEUE.prevent.command", "command", event.getMessage());
    }
}
