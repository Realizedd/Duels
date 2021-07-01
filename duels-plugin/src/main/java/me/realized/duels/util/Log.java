package me.realized.duels.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class Log {

    private static final String PLUGIN_WARN = "[%s] &c%s";
    private static final String PLUGIN_ERROR = "[%s] &4&l%s";

    private static final List<LogSource> sources = new ArrayList<>();

    private Log() {
    }

    public static void addSource(final LogSource source) {
        sources.add(source);
    }

    public static void clearSources() {
        sources.clear();
    }

    public static void info(final String s) {
        for (final LogSource source : sources) {
            source.log(Level.INFO, s);
        }
    }

    public static void info(final Loadable loadable, final String s) {
        for (final LogSource source : sources) {
            source.log(Level.INFO, loadable.getClass().getSimpleName() + ": " + s);
        }
    }

    public static void warn(final String s) {
        for (final LogSource source : sources) {
            if (source instanceof Plugin) {
                Bukkit.getConsoleSender().sendMessage(StringUtil.color(String.format(PLUGIN_WARN, ((Plugin) source).getName(), s)));
            } else {
                source.log(Level.WARNING, s);
            }
        }
    }

    public static void warn(final Loadable loadable, final String s) {
        warn(loadable.getClass().getSimpleName() + ": " + s);
    }

    public static void error(final String s, final Throwable thrown) {
        for (final LogSource source : sources) {
            if (source instanceof Plugin) {
                Bukkit.getConsoleSender().sendMessage(StringUtil.color(String.format(PLUGIN_ERROR, ((Plugin) source).getName(), s)));
            } else if (thrown != null) {
                source.log(Level.SEVERE, s, thrown);
            } else {
                source.log(Level.SEVERE, s);
            }
        }
    }

    public static void error(final String s) {
        error(s, null);
    }

    public static void error(final Loadable loadable, final String s, final Throwable thrown) {
        error(loadable.getClass().getSimpleName() + ": " + s, thrown);
    }

    public static void error(final Loadable loadable, final String s) {
        error(loadable, s, null);
    }

    public interface LogSource {

        void log(final Level level, final String s);

        void log(final Level level, final String s, final Throwable thrown);
    }
}
