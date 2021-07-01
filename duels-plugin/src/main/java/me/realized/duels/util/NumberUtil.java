package me.realized.duels.util;

import java.util.OptionalInt;

public final class NumberUtil {

    private NumberUtil() {
    }

    /**
     * Copy of {@link Integer#parseInt(String)} (String)} but returns an empty {@link OptionalInt} instead of throwing a {@link NumberFormatException}.
     *
     * @param s String to parse.
     * @return {@link OptionalInt} instance with parsed value inside or empty if string is invalid.
     */
    public static OptionalInt parseInt(final String s) {
        if (s == null) {
            return OptionalInt.empty();
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);

            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    return OptionalInt.empty();
                }

                if (len == 1) { // Cannot have lone "+" or "-"
                    return OptionalInt.empty();
                }

                i++;
            }

            multmin = limit / 10;

            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++), 10);
                if (digit < 0) {
                    return OptionalInt.empty();
                }

                if (result < multmin) {
                    return OptionalInt.empty();
                }

                result *= 10;

                if (result < limit + digit) {
                    return OptionalInt.empty();
                }

                result -= digit;
            }
        } else {
            return OptionalInt.empty();
        }

        return OptionalInt.of(negative ? result : -result);
    }
}
