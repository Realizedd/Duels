package me.realized.duels.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

    private DateUtil() {
    }

    public static String formatDate(final Date date) {
        return DATE_FORMAT.format(date);
    }

    public static String formatDatetime(final long millis) {
        return TIMESTAMP_FORMAT.format(millis);
    }

    public static String format(long seconds) {
        if (seconds <= 0) {
            return "updating...";
        }

        long years = seconds / 31556952;
        seconds -= years * 31556952;
        long months = seconds / 2592000;
        seconds -= months * 2592000;
        long weeks = seconds / 604800;
        seconds -= weeks * 604800;
        long days = seconds / 86400;
        seconds -= days * 86400;
        long hours = seconds / 3600;
        seconds -= hours * 3600;
        long minutes = seconds / 60;
        seconds -= minutes * 60;

        StringBuilder sb = new StringBuilder();

        if (years > 0) {
            sb.append(years).append("yr");
        }

        if (months > 0) {
            sb.append(months).append("mo");
        }

        if (weeks > 0) {
            sb.append(weeks).append("w");
        }

        if (days > 0) {
            sb.append(days).append("d");
        }

        if (hours > 0) {
            sb.append(hours).append("h");
        }

        if (minutes > 0) {
            sb.append(minutes).append("m");
        }

        if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }

    public static String formatMilliseconds(long ms) {
        if (ms < 1000) {
            return "0 second";
        }

        long seconds = ms / 1000 + (ms % 1000 > 0 ? 1 : 0);
        long years = seconds / 31556952;
        seconds -= years * 31556952;
        long months = seconds / 2592000;
        seconds -= months * 2592000;
        long weeks = seconds / 604800;
        seconds -= weeks * 604800;
        long days = seconds / 86400;
        seconds -= days * 86400;
        long hours = seconds / 3600;
        seconds -= hours * 3600;
        long minutes = seconds / 60;
        seconds -= minutes * 60;

        StringBuilder builder = new StringBuilder();

        if (years > 0) {
            builder.append(years).append(years > 1 ? " years" : " year");
        }

        if (months > 0) {
            if (years > 0) {
                builder.append(" ");
            }

            builder.append(months).append(months > 1 ? " months" : " month");
        }

        if (weeks > 0) {
            if (years + months > 0) {
                builder.append(" ");
            }

            builder.append(weeks).append(weeks > 1 ? " weeks" : " week");
        }

        if (days > 0) {
            if (years + months + weeks > 0) {
                builder.append(" ");
            }

            builder.append(days).append(days > 1 ? " days" : " day");
        }

        if (hours > 0) {
            if (years + months + weeks + days > 0) {
                builder.append(" ");
            }

            builder.append(hours).append(hours > 1 ? " hours" : " hour");
        }

        if (minutes > 0) {
            if (years + months + weeks + days + hours > 0) {
                builder.append(" ");
            }

            builder.append(minutes).append(minutes > 1 ? " minutes" : " minute");
        }

        if (seconds > 0) {
            if (years + months + weeks + days + hours + minutes > 0) {
                builder.append(" ");
            }

            builder.append(seconds).append(seconds > 1 ? " seconds" : " second");
        }

        return builder.toString();
    }
}
