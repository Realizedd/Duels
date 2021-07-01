package me.realized.duels.arena;

import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.event.arena.ArenaCreateEvent;
import me.realized.duels.api.event.arena.ArenaRemoveEvent;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.ArenaData;
import me.realized.duels.kit.KitImpl;
import me.realized.duels.queue.Queue;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.MultiPageGui;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ArenaManagerImpl implements Loadable, ArenaManager, Listener {

    private static final long AUTO_SAVE_INTERVAL = 20L * 60 * 5;

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File file;

    private final List<ArenaImpl> arenas = new ArrayList<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;
    private int autoSaveTask;

    public ArenaManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), "arenas.json");
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.arena-selector.title"), config.getArenaSelectorRows(), arenas);
        gui.setSpaceFiller(Items.from(config.getArenaSelectorFillerType(), config.getArenaSelectorFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.previous-page.name")).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.next-page.name")).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.empty.name")).build());
        plugin.getGuiListener().addGui(gui);

        if (config.isCdEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

        if (!file.exists()) {
            file.createNewFile();
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                final List<ArenaData> data = plugin.getGson().fromJson(reader, new TypeToken<List<ArenaData>>() {
                }.getType());

                if (data != null) {
                    for (final ArenaData arenaData : data) {
                        if (!StringUtil.isAlphanumeric(arenaData.getName())) {
                            Log.warn(this, "Excluding arena '" + arenaData.getName() + "' from load: Name is not alphanumeric.");
                            continue;
                        }

                        arenas.add(arenaData.toArena(plugin));
                    }
                }
            }
        }

        Log.info(this, "Loaded " + arenas.size() + " arena(s).");
        gui.calculatePages();

        this.autoSaveTask = plugin.doSyncRepeat(() -> {
            try {
                saveArenas();
            } catch (IOException ex) {
                Log.error(this, ex.getMessage(), ex);
            }
        }, AUTO_SAVE_INTERVAL, AUTO_SAVE_INTERVAL).getTaskId();
    }

    @Override
    public void handleUnload() throws IOException {
        plugin.cancelTask(autoSaveTask);

        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        saveArenas();
        arenas.clear();
    }

    private void saveArenas() throws IOException {
        final List<ArenaData> data = new ArrayList<>();

        for (final ArenaImpl arena : arenas) {
            data.add(new ArenaData(arena));
        }

        if (!file.exists()) {
            file.createNewFile();
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
            plugin.getGson().toJson(data, writer);
            writer.flush();
        }
    }

    @Nullable
    @Override
    public ArenaImpl get(@Nonnull final String name) {
        Objects.requireNonNull(name, "name");
        return arenas.stream().filter(arena -> arena.getName().equals(name)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public ArenaImpl get(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        return arenas.stream().filter(arena -> arena.has(player)).findFirst().orElse(null);
    }

    public boolean create(final CommandSender source, final String name) {
        if (get(name) != null) {
            return false;
        }

        final ArenaImpl arena = new ArenaImpl(plugin, name);
        arenas.add(arena);

        final ArenaCreateEvent event = new ArenaCreateEvent(source, arena);
        plugin.getServer().getPluginManager().callEvent(event);
        gui.calculatePages();
        return true;
    }

    public boolean remove(final CommandSender source, final ArenaImpl arena) {
        if (arenas.remove(arena)) {
            arena.setRemoved(true);

            final ArenaRemoveEvent event = new ArenaRemoveEvent(source, arena);
            plugin.getServer().getPluginManager().callEvent(event);
            gui.calculatePages();
            return true;
        }

        return false;
    }

    @Override
    public boolean isInMatch(@Nonnull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player) != null;
    }

    public List<ArenaImpl> getArenasImpl() {
        return arenas;
    }

    @Nonnull
    @Override
    public List<Arena> getArenas() {
        return Collections.unmodifiableList(arenas);
    }

    public Set<Player> getPlayers() {
        return arenas.stream().flatMap(arena -> arena.getPlayers().stream()).collect(Collectors.toSet());
    }

    public long getPlayersInMatch(final Queue queue) {
        return arenas.stream().filter(arena -> arena.isUsed() && arena.getMatch().isFromQueue() && arena.getMatch().getSource().equals(queue)).count() * 2;
    }

    public boolean isSelectable(final KitImpl kit, final ArenaImpl arena) {
        if (!arena.isAvailable()) {
            return false;
        }

        if (kit == null) {
            return true;
        }

        if (arena.isBoundless()) {
            return !kit.isArenaSpecific();
        }

        return arena.isBound(kit);
    }

    public ArenaImpl randomArena(final KitImpl kit) {
        final List<ArenaImpl> available = arenas.stream().filter(arena -> isSelectable(kit, arena)).collect(Collectors.toList());
        return !available.isEmpty() ? available.get(ThreadLocalRandom.current().nextInt(available.size())) : null;
    }

    public List<String> getNames() {
        return arenas.stream().map(ArenaImpl::getName).collect(Collectors.toList());
    }

    // remove bind on kit removal
    public void clearBinds(final KitImpl kit) {
        arenas.stream().filter(arena -> arena.isBound(kit)).forEach(arena -> arena.bind(kit));
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final EntityDamageEvent event) {
        if (!config.isPreventPvp() || !(event.getEntity() instanceof Player)) {
            return;
        }

        final ArenaImpl arena = get((Player) event.getEntity());

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final ProjectileLaunchEvent event) {
        if (!config.isPreventLaunchProjectile()) {
            return;
        }

        final ProjectileSource shooter = event.getEntity().getShooter();

        if (!(shooter instanceof Player)) {
            return;
        }

        final ArenaImpl arena = get((Player) shooter);

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void on(final PlayerMoveEvent event) {
        if (!config.isPreventMovement()) {
            return;
        }

        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        final ArenaImpl arena = get(event.getPlayer());

        if (arena == null || !arena.isCounting()) {
            return;
        }

        event.setTo(event.getFrom());
    }
}
