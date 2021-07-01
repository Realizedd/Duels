package me.realized.duels.queue.sign;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
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
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class QueueSignManagerImpl implements Loadable, QueueSignManager, Listener {

    private static final long AUTO_SAVE_INTERVAL = 20L * 60 * 5;

    private final DuelsPlugin plugin;
    private final Lang lang;
    private final QueueManager queueManager;
    private final File file;

    private final Map<Location, QueueSignImpl> signs = new HashMap<>();

    private int autoSaveTask;
    private int updateTask;

    public QueueSignManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.queueManager = plugin.getQueueManager();
        this.file = new File(plugin.getDataFolder(), "signs.json");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void handleLoad() throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                final List<QueueSignData> data = plugin.getGson().fromJson(reader, new TypeToken<List<QueueSignData>>() {
                }.getType());

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

        Log.info(this, "Loaded " + signs.size() + " queue sign(s).");

        this.autoSaveTask = plugin.doSyncRepeat(() -> {
            try {
                saveQueueSigns();
            } catch (IOException ex) {
                Log.error(this, ex.getMessage(), ex);
            }
        }, AUTO_SAVE_INTERVAL, AUTO_SAVE_INTERVAL).getTaskId();
        this.updateTask = plugin.doSyncRepeat(() -> signs.entrySet().removeIf(entry -> {
            entry.getValue().update();
            return entry.getValue().getQueue().isRemoved();
        }), 20L, 20L).getTaskId();
    }

    @Override
    public void handleUnload() throws Exception {
        plugin.cancelTask(autoSaveTask);
        plugin.cancelTask(updateTask);
        saveQueueSigns();
        signs.clear();
    }

    private void saveQueueSigns() throws IOException {
        final List<QueueSignData> data = new ArrayList<>();

        for (final QueueSignImpl sign : signs.values()) {
            if (sign.getQueue().isRemoved()) {
                continue;
            }

            data.add(new QueueSignData(sign));
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
    public QueueSignImpl get(@Nonnull final Sign sign) {
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
        signs.put(location, created = new QueueSignImpl(location, lang.getMessage("SIGN.format", "kit", kitName, "bet_amount", queue.getBet()), queue));
        signs.values().stream().filter(sign -> sign.equals(created)).forEach(QueueSignImpl::update);

        final QueueSignCreateEvent event = new QueueSignCreateEvent(creator, created);
        plugin.getServer().getPluginManager().callEvent(event);
        return true;
    }

    public QueueSignImpl remove(final Player source, final Location location) {
        final QueueSignImpl queueSign = signs.remove(location);

        if (queueSign == null) {
            return null;
        }

        queueSign.setRemoved(true);

        final QueueSignRemoveEvent event = new QueueSignRemoveEvent(source, queueSign);
        plugin.getServer().getPluginManager().callEvent(event);
        return queueSign;
    }

    public Collection<QueueSignImpl> getSigns() {
        return signs.values();
    }

    @Nonnull
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
