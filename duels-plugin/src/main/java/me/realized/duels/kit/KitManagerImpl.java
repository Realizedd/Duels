package me.realized.duels.kit;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.event.kit.KitCreateEvent;
import me.realized.duels.api.event.kit.KitRemoveEvent;
import me.realized.duels.api.kit.Kit;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.config.Config;
import me.realized.duels.config.Lang;
import me.realized.duels.data.KitData;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.StringUtil;
import me.realized.duels.util.compat.Items;
import me.realized.duels.util.gui.MultiPageGui;
import me.realized.duels.util.inventory.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;

public class KitManagerImpl implements Loadable, KitManager {

    private static final long AUTO_SAVE_INTERVAL = 20L * 60 * 5;

    private final DuelsPlugin plugin;
    private final Config config;
    private final Lang lang;
    private final File file;
    private final Map<String, KitImpl> kits = new LinkedHashMap<>();

    @Getter
    private MultiPageGui<DuelsPlugin> gui;
    private int autoSaveTask;

    public KitManagerImpl(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.lang = plugin.getLang();
        this.file = new File(plugin.getDataFolder(), "kits.json");
    }

    @Override
    public void handleLoad() throws IOException {
        gui = new MultiPageGui<>(plugin, lang.getMessage("GUI.kit-selector.title"), config.getKitSelectorRows(), kits.values());
        gui.setSpaceFiller(Items.from(config.getKitSelectorFillerType(), config.getKitSelectorFillerData()));
        gui.setPrevButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.previous-page.name")).build());
        gui.setNextButton(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.next-page.name")).build());
        gui.setEmptyIndicator(ItemBuilder.of(Material.PAPER).name(lang.getMessage("GUI.kit-selector.buttons.empty.name")).build());
        plugin.getGuiListener().addGui(gui);

        if (!file.exists()) {
            file.createNewFile();
        } else {
            try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
                final Map<String, KitData> data = plugin.getGson().fromJson(reader, new TypeToken<LinkedHashMap<String, KitData>>() {
                }.getType());

                if (data != null) {
                    for (final Map.Entry<String, KitData> entry : data.entrySet()) {
                        if (!StringUtil.isAlphanumeric(entry.getKey())) {
                            Log.warn(this, "Excluding kit '" + entry.getKey() + "' from load: Name is not alphanumeric.");
                            continue;
                        }

                        kits.put(entry.getKey(), entry.getValue().toKit(plugin));
                    }
                }
            }
        }

        Log.info(this, "Loaded " + kits.size() + " kit(s).");
        gui.calculatePages();

        this.autoSaveTask = plugin.doSyncRepeat(() -> {
            try {
                saveKits();
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

        saveKits();
        kits.clear();
    }

    private void saveKits() throws IOException {
        final Map<String, KitData> data = new LinkedHashMap<>();

        for (final Map.Entry<String, KitImpl> entry : kits.entrySet()) {
            data.put(entry.getKey(), new KitData(entry.getValue()));
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
    public KitImpl get(@Nonnull final String name) {
        Objects.requireNonNull(name, "name");
        return kits.get(name);
    }

    public KitImpl create(@Nonnull final Player creator, @Nonnull final String name, final boolean override) {
        Objects.requireNonNull(creator, "creator");
        Objects.requireNonNull(name, "name");

        if (!StringUtil.isAlphanumeric(name) || (!override && kits.containsKey(name))) {
            return null;
        }

        final KitImpl kit = new KitImpl(plugin, name, creator.getInventory());
        kits.put(name, kit);

        final KitCreateEvent event = new KitCreateEvent(creator, kit);
        plugin.getServer().getPluginManager().callEvent(event);
        gui.calculatePages();
        return kit;
    }

    @Nullable
    @Override
    public KitImpl create(@Nonnull final Player creator, @Nonnull final String name) {
        return create(creator, name, false);
    }

    @Nullable
    @Override
    public KitImpl remove(@Nullable CommandSender source, @Nonnull final String name) {
        Objects.requireNonNull(name, "name");

        final KitImpl kit = kits.remove(name);

        if (kit == null) {
            return null;
        }

        kit.setRemoved(true);
        plugin.getArenaManager().clearBinds(kit);

        final KitRemoveEvent event = new KitRemoveEvent(source, kit);
        plugin.getServer().getPluginManager().callEvent(event);
        gui.calculatePages();
        return kit;
    }

    @Nullable
    @Override
    public KitImpl remove(@Nonnull final String name) {
        return remove(null, name);
    }

    @Nonnull
    @Override
    public List<Kit> getKits() {
        return Collections.unmodifiableList(Lists.newArrayList(kits.values()));
    }

    public List<String> getNames(final boolean nokit) {
        final List<String> names = Lists.newArrayList();
        names.addAll(kits.keySet());

        if (nokit) {
            names.add("-"); // Special case: Change the nokit rating
        }

        return names;
    }
}
