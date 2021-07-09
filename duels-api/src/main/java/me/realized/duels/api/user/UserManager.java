package me.realized.duels.api.user;

import me.realized.duels.api.kit.Kit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the UserManager singleton used by Duels.
 */
public interface UserManager {


    /**
     * Whether or not had all users completed loading to the memory.
     *
     * @return True if all users have completed loading to the memory. False otherwise.
     */
    boolean isLoaded();


    /**
     * Gets a {@link User} with the given name.
     * Note: If {@link #isLoaded()} returns false, this may return null even if userdata file exists.
     *
     * @param name Name of the user to get.
     * @return {@link User} with the given name or null if not exists.
     */
    @Nullable
    User get(@Nonnull final String name);


    /**
     * Gets a {@link User} with the given {@link UUID}.
     * Note: If {@link #isLoaded()} returns false, this may return null even if userdata file exists.
     *
     * @param uuid {@link UUID} of the user to get.
     * @return {@link User} with the given {@link UUID} or null if not exists.
     */
    @Nullable
    User get(@Nonnull final UUID uuid);


    /**
     * Calls {@link #get(UUID)} with {@link Player#getUniqueId()}.
     *
     * @see #get(UUID)
     */
    @Nullable
    User get(@Nonnull final Player player);


    /**
     * Gets the top wins. thread-safe!
     *
     * @return {@link TopEntry} containing name and wins of the top 10 Wins or null if the leaderboard has not loaded yet.
     */
    @Nullable
    TopEntry getTopWins();


    /**
     * Gets the top losses. thread-safe!
     *
     * @return {@link TopEntry} containing name and losses of the top 10 Losses or null if the leaderboard has not loaded yet.
     */
    @Nullable
    TopEntry getTopLosses();


    /**
     * Gets the top rating for no kit. thread-safe!
     *
     * @return {@link TopEntry} containing name and rating of the top 10 Rating for no kit or null if the leaderboard has not loaded yet.
     * @since 3.3.0
     */
    @Nullable
    TopEntry getTopRatings();


    /**
     * Gets the top rating for the given {@link Kit}. thread-safe!
     *
     * @param kit {@link Kit} to get {@link TopEntry}.
     * @return {@link TopEntry} containing name and rating of the top 10 Rating for kit or null if the leaderboard has not loaded yet.
     */
    @Nullable
    TopEntry getTopRatings(@Nonnull final Kit kit);


    class TopEntry {

        private final long creation;
        private final String type, identifier;
        private final List<TopData> data;

        public TopEntry(@Nonnull final String type, @Nonnull final String identifier, @Nonnull final List<TopData> data) {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(identifier, "identifier");
            Objects.requireNonNull(data, "data");
            this.creation = System.currentTimeMillis();
            this.type = type;
            this.identifier = identifier;
            this.data = data;
        }

        public long getCreation() {
            return creation;
        }

        public String getType() {
            return type;
        }

        public String getIdentifier() {
            return identifier;
        }

        public List<TopData> getData() {
            return data;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            final TopEntry topEntry = (TopEntry) other;
            return Objects.equals(type, topEntry.type) && Objects.equals(identifier, topEntry.identifier) && Objects.equals(data, topEntry.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, identifier, data);
        }
    }

    class TopData implements Comparable<TopData> {

        private final UUID uuid;
        private final String name;
        private final int value;

        public TopData(@Nonnull final UUID uuid, @Nonnull final String name, final int value) {
            Objects.requireNonNull(uuid, "uuid");
            Objects.requireNonNull(name, "name");
            this.uuid = uuid;
            this.name = name;
            this.value = value;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public int compareTo(@Nonnull final TopData data) {
            Objects.requireNonNull(data, "data");
            return Integer.compare(value, data.value);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            final TopData topData = (TopData) other;
            return value == topData.value && Objects.equals(uuid, topData.uuid) && Objects.equals(name, topData.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, name, value);
        }
    }
}
