package me.realized.duels.util;

import java.util.UUID;
import java.util.regex.Pattern;

public final class UUIDUtil {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    private UUIDUtil() {
    }

    public static UUID parseUUID(final String s) {
        if (s == null || !UUID_PATTERN.matcher(s).matches()) {
            return null;
        }

        return UUID.fromString(s);
    }
}
