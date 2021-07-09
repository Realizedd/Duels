package me.realized.duels.api;

import me.realized.duels.api.arena.ArenaManager;
import me.realized.duels.api.command.SubCommand;
import me.realized.duels.api.kit.KitManager;
import me.realized.duels.api.queue.DQueueManager;
import me.realized.duels.api.queue.sign.QueueSignManager;
import me.realized.duels.api.spectate.SpectateManager;
import me.realized.duels.api.user.UserManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;


public interface Duels extends Plugin {

    /**
     * Gets the UserManager singleton used by Duels.
     *
     * @return UserManager singleton
     */
    @Nonnull
    UserManager getUserManager();


    /**
     * Gets the ArenaManager singleton used by Duels.
     *
     * @return ArenaManager singleton
     */
    @Nonnull
    ArenaManager getArenaManager();


    /**
     * Gets the KitManager singleton used by Duels.
     *
     * @return KitManager singleton
     */
    @Nonnull
    KitManager getKitManager();


    /**
     * Gets the SpectateManager singleton used by Duels.
     *
     * @return SpectateManager singleton
     * @since 3.4.1
     */
    @Nonnull
    SpectateManager getSpectateManager();


    /**
     * Gets the DQueueManager singleton used by Duels.
     *
     * @return DQueueManager singleton
     */
    @Nonnull
    DQueueManager getQueueManager();


    /**
     * Gets the QueueSignManager singleton used by Duels.
     *
     * @return QueueSignManager singleton
     */
    @Nonnull
    QueueSignManager getQueueSignManager();


    /**
     * Registers a {@link SubCommand} to a Command registered by Duels.
     *
     * @param command    Name of the parent command to register the {@link SubCommand}.
     * @param subCommand {@link SubCommand} to register.
     * @return True if sub command was successfully registered. False otherwise.
     */
    boolean registerSubCommand(@Nonnull final String command, @Nonnull final SubCommand subCommand);


    /**
     * Registers a {@link Listener} that will be automatically unregistered on unload of Duels.
     *
     * @param listener {@link Listener} to register.
     * @since 3.1.2
     */
    void registerListener(@Nonnull final Listener listener);


    /**
     * Reloads the plugin.
     *
     * @return True if reload was successful. False otherwise.
     */
    boolean reload();


    /**
     * Runs the task on server thread.
     *
     * @param task Task to run.
     * @return BukkitTask executed.
     * @since 3.1.0
     */
    BukkitTask doSync(@Nonnull final Runnable task);


    /**
     * Runs the task after delay on server thread.
     *
     * @param task  Task to run.
     * @param delay time to delay. 20L = 1s
     * @return BukkitTask executed.
     * @since 3.1.0
     */
    BukkitTask doSyncAfter(@Nonnull final Runnable task, long delay);


    /**
     * Runs the task after delay on server thread repeatedly.
     *
     * @param task     Task to run.
     * @param delay    time to delay the start of repeat. 20L = 1s
     * @param interval interval of this task. 20L = 1s
     * @return BukkitTask executed.
     * @since 3.1.0
     */
    BukkitTask doSyncRepeat(@Nonnull final Runnable task, long delay, long interval);


    /**
     * Runs the task asynchronously.
     *
     * @param task Task to run asynchronously.
     * @return BukkitTask executed.
     * @since 3.1.0
     */
    BukkitTask doAsync(@Nonnull final Runnable task);


    /**
     * Runs the task after delay asynchronously.
     *
     * @param task  Task to run asynchronously.
     * @param delay time to delay. 20L = 1s
     * @return BukkitTask executed.
     * @since 3.1.0
     */
    BukkitTask doAsyncAfter(@Nonnull final Runnable task, long delay);


    /**
     * Runs the task after delay asynchronously repeatedly.
     *
     * @param task     Task to run asynchronously.
     * @param delay    time to delay the start of repeat. 20L = 1s
     * @param interval interval of this task. 20L = 1s
     * @return BukkitTask executed.
     * @since 3.1.0
     */
    BukkitTask doAsyncRepeat(@Nonnull final Runnable task, long delay, long interval);


    /**
     * Cancels the task if not already cancelled.
     *
     * @param task Task to cancel if not already cancelled.
     * @since 3.2.0
     */
    void cancelTask(@Nonnull final BukkitTask task);


    /**
     * Cancels a task with id if found and running.
     *
     * @param id Id of the task to cancel.
     * @since 3.2.0
     */
    void cancelTask(final int id);


    /**
     * Logs a message with {@link java.util.logging.Level#INFO}.
     *
     * @param message message to log.
     * @since 3.1.0
     */
    void info(@Nonnull final String message);


    /**
     * Logs a message with {@link java.util.logging.Level#WARNING}.
     *
     * @param message message to log.
     * @since 3.1.0
     */
    void warn(@Nonnull final String message);


    /**
     * Logs a message with {@link java.util.logging.Level#SEVERE}.
     *
     * @param message message to log.
     * @since 3.1.0
     */
    void error(@Nonnull final String message);


    /**
     * Logs a message and the {@link Throwable} provided with {@link java.util.logging.Level#SEVERE}.
     *
     * @param message message to log.
     * @param thrown  {@link Throwable} to log.
     * @since 3.1.0
     */
    void error(@Nonnull final String message, @Nonnull Throwable thrown);


    /**
     * Current plugin version.
     *
     * @return version of the plugin.
     * @since 3.1.0
     */
    String getVersion();
}
