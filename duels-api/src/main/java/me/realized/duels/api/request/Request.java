package me.realized.duels.api.request;

import java.util.UUID;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.kit.Kit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Request sent.
 */
public interface Request {

    /**
     * The {@link UUID} of sender of this {@link Request}.
     *
     * @return Never-null {@link UUID} of the sender of this {@link Request}.
     */
    @NotNull
    UUID getSender();


    /**
     * The {@link UUID} of the receiver of this {@link Request}.
     *
     * @return Never-null {@link UUID} of the receiver of this {@link Request}.
     */
    @NotNull
    UUID getTarget();


    /**
     * The {@link Kit} for this {@link Request} or null if no {@link Kit} was selected.
     *
     * @return {@link Kit} for this {@link Request} or null if no {@link Kit} was selected.
     */
    @Nullable
    Kit getKit();


    /**
     * The {@link Arena} for this {@link Request} or null if no {@link Arena} was selected.
     *
     * @return {@link Arena} for this {@link Request} or null if no {@link Arena} was selected.
     */
    @Nullable
    Arena getArena();


    /**
     * Whether or not item betting is enabled for this {@link Request}.
     *
     * @return True if item betting is enabled for this {@link  Request}. False otherwise.
     */
    boolean canBetItems();


    /**
     * The bet for this {@link Request}.
     *
     * @return Bet amount for this {@link Request} or 0 if not specified.
     */
    double getBet();
}
