package me.realized.duels.queue.sign;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.api.event.queue.sign.QueueSignCreateEvent;
import me.realized.duels.api.event.queue.sign.QueueSignRemoveEvent;
import me.realized.duels.api.queue.sign.QueueSign;
import me.realized.duels.api.queue.sign.QueueSignManager;
import me.realized.duels.config.Lang;
import me.realized.duels.data.QueueSignData;
import me.realized.duels.queue.Queue;
import me.realized.duels.queue.QueueManager;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.NumberUtil;
import me.realized.duels.util.io.FileUtil;
import me.realized.duels.util.json.JsonUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QueueSignManagerImpl implements Loadable, QueueSignManager, Listener {

    private static final String FILE_NAME = "signs.json";

    private static final String SIGNS_LOADED = "Loaded %s queue sign(s).";

    private final DuelsPlugin plugin;
    private final Lang lang;
    private final QueueManager queueManager;
    private final File file;

    private final Map<Location, QueueSignImpl> signs = new HashMap<>();

    private int updateTask;

    public QueueSignManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.queueManager = plugin.getQueueManager();
        this.file = new File(plugin.getDataFolder(), FILE_NAME);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws IOException {
        if (FileUtil.checkNonEmpty(file, true)) {
            try (final Reader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
                final List<QueueSignData> data = JsonUtil.getObjectMapper().readValue(reader, new TypeReference<List<QueueSignData>>() {});

                if (data != null) {
                    data.forEach(queueSignData -> {
                        final QueueSignImpl queueSign = queueSignData.toQueueSign(plugin);

                        if (queueSign != null) {
                            signs.put(queueSign.getLocation(), queueSign);
                        }
                    });
                }
            }
        }

        Log.info(this, String.format(SIGNS_LOADED, signs.size()));

        this.updateTask = plugin.doSyncRepeat(() -> signs.entrySet().removeIf(entry -> {
            entry.getValue().update();
            return entry.getValue().getQueue().isRemoved();
        }), 20L, 20L).getTaskId();
    }

    @Override
    public void handleUnload() {
        plugin.cancelTask(updateTask);
        signs.clear();
    }

    private void saveQueueSigns() {
        final List<QueueSignData> data = new ArrayList<>();

        for (final QueueSignImpl sign : signs.values()) {
            if (sign.getQueue().isRemoved()) {
                continue;
            }

            data.add(new QueueSignData(sign));
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
    public QueueSignImpl get(@NotNull final Sign sign) {
        Objects.requireNonNull(sign, "sign");
        return get(sign.getLocation());
    }

    public QueueSignImpl get(final Location location) {
        return signs.get(location);
    }

    public boolean create(final Player creator, final Location location, final Queue queue) {
        if (get(location) != null) {
            return false;
        }

        final QueueSignImpl created;
        final String kitName = queue.getKit() != null ? queue.getKit().getName() : lang.getMessage("GENERAL.none");
        signs.put(location, created = new QueueSignImpl(location, lang.getMessage("SIGN.format",
                "kit", kitName,
                "bet_amount", NumberUtil.formatDouble(queue.getBet())
        ), queue));
        signs.values().stream().filter(sign -> sign.equals(created)).forEach(QueueSignImpl::update);
        saveQueueSigns();

        final QueueSignCreateEvent event = new QueueSignCreateEvent(creator, created);
        Bukkit.getPluginManager().callEvent(event);
        return true;
    }

    public QueueSignImpl remove(final Player source, final Location location) {
        final QueueSignImpl queueSign = signs.remove(location);

        if (queueSign == null) {
            return null;
        }

        queueSign.setRemoved(true);
        saveQueueSigns();

        final QueueSignRemoveEvent event = new QueueSignRemoveEvent(source, queueSign);
        Bukkit.getPluginManager().callEvent(event);
        return queueSign;
    }

    public Collection<QueueSignImpl> getSigns() {
        return signs.values();
    }

    @NotNull
    @Override
    public List<QueueSign> getQueueSigns() {
        return Lists.newArrayList(getSigns());
    }

    @EventHandler
    public void on(final PlayerInteractEvent event) {
        final Block block;

        if (!event.hasBlock() || !((block = event.getClickedBlock()).getState() instanceof Sign)) {
            return;
        }

        final Player player = event.getPlayer();
        final QueueSignImpl sign = get(block.getLocation());

        if (sign == null || !queueManager.queue(player, sign.getQueue())) {
            return;
        }

        signs.values().stream().filter(queueSign -> queueSign.equals(sign)).forEach(QueueSignImpl::update);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(final BlockBreakEvent event) {
        final Block block = event.getBlock();

        if (!(block.getState() instanceof Sign) || get(block.getLocation()) == null) {
            return;
        }

        final Player player = event.getPlayer();

        if (!player.hasPermission(Permissions.ADMIN)) {
            lang.sendMessage(player, "ERROR.no-permission", "permission", Permissions.ADMIN);
            return;
        }

        lang.sendMessage(player, "ERROR.sign.cancel-break");
        event.setCancelled(true);
    }
}
