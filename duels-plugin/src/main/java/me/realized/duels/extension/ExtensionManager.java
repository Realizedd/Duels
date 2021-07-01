package me.realized.duels.extension;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.api.Duels;
import me.realized.duels.api.extension.DuelsExtension;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import me.realized.duels.util.NumberUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ExtensionManager implements Loadable {

    private static Method INIT_EXTENSION;

    static {
        try {
            INIT_EXTENSION = DuelsExtension.class.getDeclaredMethod("init", Duels.class, String.class, File.class, File.class);
            INIT_EXTENSION.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
        }
    }

    private final Map<String, DuelsExtension> extensions = new HashMap<>();
    private final Map<DuelsExtension, ExtensionInfo> info = new HashMap<>();

    private final DuelsPlugin plugin;
    private final File folder;

    public ExtensionManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "extensions");

        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    @Override
    public void handleLoad() {
        final File[] jars = folder.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jars == null) {
            return;
        }

        for (final File file : jars) {
            try {
                final ExtensionInfo info = new ExtensionInfo(file);

                if (extensions.containsKey(info.getName())) {
                    Log.error(this, "Could not load extension " + file.getName() + ": An extension with the name '" + info.getName() + "' already exists");
                    continue;
                }

                if (!info.getDepends().isEmpty() && info.getDepends().stream().anyMatch(depend -> !plugin.getServer().getPluginManager().isPluginEnabled(depend))) {
                    Log.error(this, "Could not load extension " + file.getName() + ": This extension require the following plugins to enable - " + info.getDepends());
                    continue;
                }

                if (info.getApiVersion() != null && isLower(plugin.getVersion(), info.getApiVersion())) {
                    Log.error(this, "Could not load extension " + file.getName() + ": This extension requires Duels v" + info.getApiVersion() + " or higher!");
                    continue;
                }

                final ExtensionClassLoader classLoader = new ExtensionClassLoader(file, info, DuelsExtension.class.getClassLoader());
                final DuelsExtension extension = classLoader.getExtension();

                if (extension == null) {
                    Log.error(this, "Could not load extension " + file.getName() + ": Failed to initiate main class");
                    continue;
                }

                final String requiredVersion = extension.getRequiredVersion();

                if (requiredVersion != null && isLower(plugin.getVersion(), requiredVersion)) {
                    Log.error(this, "Could not load extension " + file.getName() + ": This extension requires Duels v" + requiredVersion + " or higher!");
                    continue;
                }

                INIT_EXTENSION.invoke(extension, plugin, info.getName(), folder, file);
                extension.setEnabled(true);
                Log.info(this, "Extension '" + extension.getName() + "' is now enabled.");
                extensions.put(extension.getName(), extension);
                this.info.put(extension, info);
            } catch (Throwable thrown) {
                Log.error(this, "Could not enable extension " + file.getName() + "!", thrown);
            }
        }
    }

    private boolean isLower(String version, String otherVersion) {
        version = version.replace("-SNAPSHOT", "").replace(".", "");
        otherVersion = otherVersion.replace("-SNAPSHOT", "").replace(".", "");
        return NumberUtil.parseInt(version).orElse(0) < NumberUtil.parseInt(otherVersion).orElse(0);
    }

    @Override
    public void handleUnload() {
        extensions.values().forEach(extension -> {
            try {
                extension.setEnabled(false);
                final ClassLoader classLoader = extension.getClass().getClassLoader();

                if (classLoader instanceof ExtensionClassLoader) {
                    ((ExtensionClassLoader) classLoader).close();
                }

                Log.info(this, "Extension '" + extension.getName() + "' is now disabled.");
            } catch (Exception ex) {
                Log.error(this, "Could not disable extension " + extension.getName() + "!", ex);
            }
        });
        extensions.clear();
        info.clear();
    }

    public DuelsExtension getExtension(final String name) {
        return extensions.get(name);
    }

    public ExtensionInfo getInfo(final DuelsExtension extension) {
        return info.get(extension);
    }
}
