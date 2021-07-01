package me.realized.duels.util;

public final class RatingUtil {

    private RatingUtil() {
    }

    public static int getChange(final int k, final int winnerRating, final int loserRating) {
        final double wr = r(winnerRating);
        final double lr = r(loserRating);
        return (int) Math.floor(k * (1 - (wr / (wr + lr))));
    }

    private static double r(final int rating) {
        return Math.pow(10.0, rating / 400.0);
    }
}
