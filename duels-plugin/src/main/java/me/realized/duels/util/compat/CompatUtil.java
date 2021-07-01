package me.realized.duels.util.compat;

import me.realized.duels.util.NumberUtil;
import org.bukkit.Bukkit;

public final class CompatUtil {

    private static final int SUB_VERSION;

    static {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        final String[] versionInfo = packageName.substring(packageName.lastIndexOf('.') + 1).split("_");
        SUB_VERSION = NumberUtil.parseInt(versionInfo[1]).orElse(0);
    }

    private CompatUtil() {
    }

    public static boolean is1_13() {
        return SUB_VERSION == 13;
    }

    public static boolean isPre1_14() {
        return SUB_VERSION < 14;
    }

    public static boolean isPre1_13() {
        return SUB_VERSION < 13;
    }

    public static boolean isPre1_12() {
        return SUB_VERSION < 12;
    }

    public static boolean isPre1_9() {
        return SUB_VERSION < 9;
    }

    public static boolean isPre1_8() {
        return SUB_VERSION < 8;
    }
}
