package me.realized.duels.api.user;

import me.realized.duels.api.kit.Kit;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * Represents a User loaded on the server.
 */
public interface User {

    /**
     * The {@link UUID} of this user. thread-safe!
     *
     * @return {@link UUID} of this user.
     */
    @Nonnull
    UUID getUuid();


    /**
     * The Name of this user. thread-safe!
     *
     * @return Name of this user.
     */
    @Nonnull
    String getName();


    /**
     * Total wins of this user. thread-safe!
     *
     * @return total wins of this user.
     */
    int getWins();


    /**
     * Sets new total wins for this user
     *
     * @param wins New total wins.
     */
    void setWins(final int wins);


    /**
     * Total losses of this user. thread-safe!
     *
     * @return total losses of this user.
     */
    int getLosses();


    /**
     * Sets new total wins for this user.
     *
     * @param losses New total losses.
     */
    void setLosses(final int losses);


    /**
     * Whether or not this user is receiving duel requests.
     *
     * @return True if this user has requests enabled. False otherwise.
     */
    boolean canRequest();


    /**
     * Enables or disables duel requests for this user.
     *
     * @param requests True to allow sending duel requests to this user. False otherwise.
     */
    void setRequests(final boolean requests);


    /**
     * UnmodifiableList of recent matches for this user.
     *
     * @return Never-null UnmodifiableList of recent matches for this user.
     */
    @Nonnull
    List<MatchInfo> getMatches();


    /**
     * Gets the no kit rating. thread-safe!
     *
     * @return no kit rating.
     * @since 3.3.0
     */
    int getRating();


    /**
     * Gets the rating of the given {@link Kit}. thread-safe!
     *
     * @param kit {@link Kit} to check for rating.
     * @return Rating for this {@link Kit}.
     */
    int getRating(@Nonnull final Kit kit);


    /**
     * Resets the rating to default for the no kit rating. thread-safe!
     *
     * @since 3.3.0
     */
    void resetRating();


    /**
     * Resets the rating to default for the given {@link Kit}. thread-safe!
     *
     * @param kit {@link Kit} to reset the rating to default.
     */
    void resetRating(@Nonnull final Kit kit);


    /**
     * Resets user's wins, losses, recent matches, and all rating.
     */
    void reset();
}
