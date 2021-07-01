package me.realized.duels.util;

import java.util.Arrays;

public final class EnumUtil {

    private EnumUtil() {
    }

    public static <E extends Enum<E>> E getByName(final String name, Class<E> clazz) {
        return clazz.cast(Arrays.stream(clazz.getEnumConstants()).filter(type -> type.name().equalsIgnoreCase(name)).findFirst().orElse(null));
    }
}
