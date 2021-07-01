package me.realized.duels.util;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class StringUtil {

    private static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final TreeMap<Integer, String> ROMAN_NUMERALS = new TreeMap<>();

    static {
        ROMAN_NUMERALS.put(1000, "M");
        ROMAN_NUMERALS.put(900, "CM");
        ROMAN_NUMERALS.put(500, "D");
        ROMAN_NUMERALS.put(400, "CD");
        ROMAN_NUMERALS.put(100, "C");
        ROMAN_NUMERALS.put(90, "XC");
        ROMAN_NUMERALS.put(50, "L");
        ROMAN_NUMERALS.put(40, "XL");
        ROMAN_NUMERALS.put(10, "X");
        ROMAN_NUMERALS.put(9, "IX");
        ROMAN_NUMERALS.put(5, "V");
        ROMAN_NUMERALS.put(4, "IV");
        ROMAN_NUMERALS.put(1, "I");
    }

    private StringUtil() {
    }

    // Source: https://stackoverflow.com/questions/12967896/converting-integers-to-roman-numerals-java
    public static String toRoman(final int number) {
        if (number <= 0) {
            return String.valueOf(number);
        }

        int key = ROMAN_NUMERALS.floorKey(number);

        if (number == key) {
            return ROMAN_NUMERALS.get(number);
        }

        return ROMAN_NUMERALS.get(key) + toRoman(number - key);
    }

    public static String fromList(final List<?> list) {
        StringBuilder builder = new StringBuilder();

        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                builder.append(list.get(i).toString()).append(i + 1 != list.size() ? "\n" : "");
            }
        }

        return builder.toString();
    }

    public static String parse(final Location location) {
        return "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
    }

    public static String reverseColor(final String s) {
        return s.replace(ChatColor.COLOR_CHAR, '&');
    }

    public static List<String> reverseColor(List<String> input) {
        // In case input is an unmodifiable list
        input = Lists.newArrayList(input);
        input.replaceAll(s -> s = reverseColor(s));
        return input;
    }

    public static String color(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static List<String> color(final List<String> input) {
        return color(input, null);
    }

    public static List<String> color(final List<String> input, final Function<String, String> function) {
        input.replaceAll(s -> s = color(function != null ? function.apply(s) : s));
        return input;
    }

    public static boolean isAlphanumeric(final String input) {
        return ALPHANUMERIC.matcher(input.replace(" ", "")).matches();
    }
}
