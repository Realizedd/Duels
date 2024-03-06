package me.realized.duels.arena;

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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
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
import me.realized.duels.util.io.FileUtil;
import me.realized.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArenaManagerImpl implements Loadable, ArenaManager {

    private static final String FILE_NAME = "arenas.json";

    private static final String ERROR_NOT_ALPHANUMERIC = "Could not load arena %s: Name is not alphanumeric.";
    private static final String ARENAS_LOADED = "Loaded %s arena(s).";

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File file;

    private final List<ArenaImpl> arenas = new ArrayList<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;

    public ArenaManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);

        Bukkit.getPluginManager().registerEvents(new ArenaListener(), plugin);
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.arena-selector.title"), config.getArenaSelectorRows(), arenas);
        gui.setSpaceFiller(Items.from(config.getArenaSelectorFillerType(), config.getArenaSelectorFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.previous-page.name")).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.next-page.name")).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.empty.name")).build());
        plugin.getGuiListener().addGui(gui);

        if (FileUtil.checkNonEmpty(file, true)) {
            try (final Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
                final List<ArenaData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<List<ArenaData>>() {});

                if (data != null) {
                    for (final ArenaData arenaData : data) {
                        if (!StringUtil.isAlphanumeric(arenaData.getName())) {
                            Log.warn(this, String.format(ERROR_NOT_ALPHANUMERIC, arenaData.getName()));
                            continue;
                        }

                        arenas.add(arenaData.toArena(plugin));
                    }
                }
            }
        }

        Log.info(this, String.format(ARENAS_LOADED, arenas.size()));
        gui.calculatePages();
    }

    @Override
    public void handleUnload() {
        if (gui != null) {
            plugin.getGuiListener().removeGui(gui);
        }

        arenas.clear();
    }

    void saveArenas() {
        final List<ArenaData> data = new ArrayList<>();

        for (final ArenaImpl arena : arenas) {
            data.add(new ArenaData(arena));
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
    public ArenaImpl get(@NotNull final String name) {
        Objects.requireNonNull(name, "name");
        return arenas.stream().filter(arena -> arena.getName().equals(name)).findFirst().orElse(null);
    }

    @Nullable
    @Override
    public ArenaImpl get(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return arenas.stream().filter(arena -> arena.has(player)).findFirst().orElse(null);
    }

    @Override
    public boolean isInMatch(@NotNull final Player player) {
        Objects.requireNonNull(player, "player");
        return get(player) != null;
    }

    @NotNull
    @Override
    public List<Arena> getArenas() {
        return Collections.unmodifiableList(arenas);
    }

    public boolean create(final CommandSender source, final String name) {
        if (get(name) != null) {
            return false;
        }

        final ArenaImpl arena = new ArenaImpl(plugin, name);
        arenas.add(arena);
        saveArenas();

        final ArenaCreateEvent event = new ArenaCreateEvent(source, arena);
        Bukkit.getPluginManager().callEvent(event);
        gui.calculatePages();
        return true;
    }

    public boolean remove(final CommandSender source, final ArenaImpl arena) {
        if (arenas.remove(arena)) {
            arena.setRemoved(true);
            saveArenas();

            final ArenaRemoveEvent event = new ArenaRemoveEvent(source, arena);
            Bukkit.getPluginManager().callEvent(event);
            gui.calculatePages();
            return true;
        }

        return false;
    }

    public List<ArenaImpl> getArenasImpl() {
        return arenas;
    }

    public Set<Player> getPlayers() {
        return arenas.stream().flatMap(arena -> arena.getPlayers().stream()).collect(Collectors.toSet());
    }

    public long getPlayersInMatch(final Queue queue) {
        return arenas.stream().filter(arena -> arena.isUsed() && arena.getMatch().isFromQueue() && arena.getMatch().getSource().equals(queue)).count() * 2;
    }

    public boolean isSelectable(@Nullable final KitImpl kit, @NotNull final ArenaImpl arena) {
        if (!arena.isAvailable()) {
            return false;
        }

        if (arena.isBoundless()) {
            if (kit == null) {
                return true;
            } else {
                return !kit.isArenaSpecific();
            }
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

    // Called on kit removal
    public void clearBinds(final KitImpl kit) {
        arenas.stream().filter(arena -> arena.isBound(kit)).forEach(arena -> arena.bind(kit));
    }

    private class ArenaListener implements Listener {

        @EventHandler
        public void on(final PlayerInteractEvent event) {
            if(event.getAction() != Action.RIGHT_CLICK_AIR && event.isCancelled() || !config.isPreventInteract()) {
                return;
            }

            Player player = event.getPlayer();

            final ArenaImpl arena = get(player);

            if (arena == null || !arena.isCounting()) {
                return;
            }

            event.setCancelled(true);
            player.updateInventory();
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

            if (arena == null || !arena.isCounting() || event.getEntity().getClass().getName().contains("Potion")) {
                return;
            }

            event.setCancelled(true);
        }

        @EventHandler(ignoreCancelled = true)
        public void on(final PlayerMoveEvent event) {
            final Location to = event.getTo();
            if(to.getBlockY() < config.getMinY()) {
                event.getPlayer().damage(99999);
            }

            if (!config.isPreventMovement()) {
                return;
            }

            final Location from = event.getFrom();

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
}
