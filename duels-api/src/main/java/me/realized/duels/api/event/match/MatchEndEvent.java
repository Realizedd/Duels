package me.realized.duels.api.event.match;

import java.util.Objects;
import java.util.UUID;
import me.realized.duels.api.match.Match;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a {@link Match} is ending.
 *
 * Note: {@link Match#isFinished()} will return true only after this event is called.
 */
public class MatchEndEvent extends MatchEvent {

    private final UUID winner, loser;
    private final Reason reason;

    public MatchEndEvent(@NotNull final Match match, @Nullable final UUID winner, @Nullable final UUID loser, @NotNull final Reason reason) {
        super(match);
        Objects.requireNonNull(reason, "reason");
        this.winner = winner;
        this.loser = loser;
        this.reason = reason;
    }

    /**
     * Winner of the {@link Match}. May be null if there was a no winner!
     *
     * @return {@link UUID} of the winner of the {@link Match}. May be null if there was a no winner!
     */
    @Nullable
    public UUID getWinner() {
        return winner;
    }

    /**
     * Loser of the {@link Match}. May be null if there was a no loser!
     *
     * @return {@link UUID} of the loser of the {@link Match}. May be null if there was a no loser!
     */
    @Nullable
    public UUID getLoser() {
        return loser;
    }

    /**
     * End reason of the {@link Match}. If reason is {@link Reason#OPPONENT_DEFEAT}, {@link #getWinner()} and {@link #getLoser()} is guaranteed to not return null.
     *
     * @return {@link Reason} that is the end reason of the {@link Match}.
     */
    @NotNull
    public Reason getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public enum Reason {

        OPPONENT_DEFEAT,
        TIE,
        MAX_TIME_REACHED,
        PLUGIN_DISABLE,
        OTHER
    }
}
