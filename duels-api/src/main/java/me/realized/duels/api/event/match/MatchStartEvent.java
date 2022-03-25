package me.realized.duels.api.event.match;

import java.util.Objects;
import me.realized.duels.api.match.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Match} has started.
 */
public class MatchStartEvent extends MatchEvent {

    private final Player[] players;

    public MatchStartEvent(@NotNull final Match match, @NotNull final Player... players) {
        super(match);
        Objects.requireNonNull(players, "players");
        this.players = players;
    }

    /**
     * The starters of the {@link Match}.
     *
     * @return Never-null {@link Player} array representing the starters of the {@link Match}.
     */
    @NotNull
    public Player[] getPlayers() {
        return players;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }
}
