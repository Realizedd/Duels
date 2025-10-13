package me.realized.duels.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.converters.ConfigConverter9_10;
import me.realized.duels.util.EnumUtil;
import me.realized.duels.util.config.AbstractConfiguration;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Config extends AbstractConfiguration<DuelsPlugin> {

    @Getter
    private int version;
    @Getter
    private boolean checkForUpdates;

    @Getter
    private boolean ctpPreventDuel;
    @Getter
    private boolean ctpPreventTag;
    @Getter
    private boolean arPreventDuel;
    @Getter
    private boolean arPreventTag;
    @Getter
    private boolean pmPreventDuel;
    @Getter
    private boolean pmPreventTag;
    @Getter
    private boolean clxPreventDuel;
    @Getter
    private boolean clxPreventTag;
    @Getter
    private boolean autoUnvanish;
    @Getter
    private boolean setBackLocation;
    @Getter
    private boolean disableSkills;
    @Getter
    private boolean fuNoPowerLoss;
    @Getter
    private boolean fNoPowerLoss;
    @Getter
    private boolean duelzoneEnabled;
    @Getter
    private List<String> duelzones;
    @Getter
    private boolean myPetDespawn;
    @Getter
    private boolean preventBountyLoss;
    @Getter
    private boolean preventKDRChange;
    @Getter
    private String lhWinsCmd;
    @Getter
    private String lhWinsTitle;
    @Getter
    private String lhLossesCmd;
    @Getter
    private String lhLossesTitle;

    @Getter
    private boolean requiresClearedInventory;
    @Getter
    private boolean preventCreativeMode;
    @Getter
    private boolean ownInventoryEnabled;
    @Getter
    private boolean ownInventoryDropInventoryItems;
    @Getter
    private boolean ownInventoryUsePermission;
    @Getter
    private boolean kitSelectingEnabled;
    @Getter
    private boolean kitSelectingUsePermission;
    @Getter
    private boolean arenaSelectingEnabled;
    @Getter
    private boolean arenaSelectingUsePermission;
    @Getter
    private boolean itemBettingEnabled;
    @Getter
    private boolean itemBettingUsePermission;
    @Getter
    private boolean moneyBettingEnabled;
    @Getter
    private boolean moneyBettingUsePermission;
    @Getter
    private int expiration;

    @Getter
    private int maxDuration;
    @Getter
    private boolean startCommandsEnabled;
    @Getter
    private boolean startCommandsQueueOnly;
    @Getter
    private int minY;
    @Getter
    private List<String> startCommands;
    @Getter
    private boolean endCommandsEnabled;
    @Getter
    private boolean endCommandsQueueOnly;
    @Getter
    private List<String> endCommands;
    @Getter
    private boolean tieCommandsEnabled;
    @Getter
    private boolean tieCommandsQueueOnly;
    @Getter
    private List<String> tieCommands;
    @Getter
    private boolean projectileHitMessageEnabled;
    @Getter
    private List<String> projectileHitMessageTypes;
    @Getter
    private boolean preventInventoryOpen;
    @Getter
    private boolean protectKitItems;
    @Getter
    private boolean removeEmptyBottle;
    @Getter
    private boolean preventTpToMatchPlayers;
    @Getter
    private boolean forceAllowCombat;
    @Getter
    private boolean cancelIfMoved;
    @Getter
    private List<String> blacklistedWorlds;
    @Getter
    private boolean teleportToLastLocation;
    @Getter
    private int teleportDelay;
    @Getter
    private boolean spawnFirework;
    @Getter
    private boolean arenaOnlyEndMessage;
    @Getter
    private boolean displayInventories;
    @Getter
    private boolean preventItemDrop;
    @Getter
    private boolean clearItemsAfterMatch;
    @Getter
    private boolean preventItemPickup;
    @Getter
    private boolean limitTeleportEnabled;
    @Getter
    private double distanceAllowed;
    @Getter
    private boolean blockAllCommands;
    @Getter
    private List<String> whitelistedCommands;
    @Getter
    private List<String> blacklistedCommands;

    @Getter
    private List<String> queueBlacklistedCommands;

    @Getter
    private boolean ratingEnabled;
    @Getter
    private int kFactor;
    @Getter
    private int defaultRating;
    @Getter
    private boolean ratingQueueOnly;

    @Getter
    private boolean specRequiresClearedInventory;
    @Getter
    private boolean specUseSpectatorGamemode;
    @Getter
    private boolean specAddInvisibilityEffect;
    @Getter
    private List<String> specWhitelistedCommands;

    @Getter
    private boolean cdEnabled;
    @Getter
    private List<String> cdMessages;
    @Getter
    private List<String> titles;
    @Getter
    private boolean preventMovement;
    @Getter
    private boolean preventLaunchProjectile;
    @Getter
    private boolean preventPvp;
    @Getter
    private boolean preventInteract;

    @Getter
    private boolean displayKitRatings;
    @Getter
    private boolean displayNoKitRating;
    @Getter
    private boolean displayPastMatches;
    @Getter
    private int matchesToDisplay;

    @Getter
    private long topUpdateInterval;
    @Getter
    private String topWinsType;
    @Getter
    private String topWinsIdentifier;
    @Getter
    private String topLossesType;
    @Getter
    private String topLossesIdentifier;
    @Getter
    private String topKitType;
    @Getter
    private String topKitIdentifier;
    @Getter
    private String topNoKitType;
    @Getter
    private String topNoKitIdentifier;

    @Getter
    private int kitSelectorRows;
    @Getter
    private String kitSelectorFillerType;
    @Getter
    private short kitSelectorFillerData;
    @Getter
    private int arenaSelectorRows;
    @Getter
    private String arenaSelectorFillerType;
    @Getter
    private short arenaSelectorFillerData;
    @Getter
    private String settingsFillerType;
    @Getter
    private short settingsFillerData;
    @Getter
    private int queuesRows;
    @Getter
    private String queuesFillerType;
    @Getter
    private short queuesFillerData;
    @Getter
    private boolean inheritKitItemType;

    @Getter
    private double soupHeartsToRegen;
    @Getter
    private boolean soupRemoveEmptyBowl;
    @Getter
    private boolean soupCancelIfAlreadyFull;

    private final Map<String, MessageSound> sounds = new HashMap<>();

    public Config(final DuelsPlugin plugin) {
        super(plugin, "config");
    }

    @Override
    protected void loadValues(FileConfiguration configuration) throws Exception {
        final int prevVersion = configuration.getInt("config-version", 0);

        if (prevVersion < 10) {
            configuration = convert(new ConfigConverter9_10());
        } else if (prevVersion < getLatestVersion()) {
            configuration = convert(null);
        }

        version = configuration.getInt("config-version");
        checkForUpdates = configuration.getBoolean("check-for-updates", true);

        ctpPreventDuel = configuration.getBoolean("supported-plugins.CombatTagPlus.prevent-duel-if-tagged", true);
        ctpPreventTag = configuration.getBoolean("supported-plugins.CombatTagPlus.prevent-tag-in-duel", true);
        arPreventDuel = configuration.getBoolean("supported-plugins.AntiRelog.prevent-duel-if-tagged", true);
        arPreventTag = configuration.getBoolean("supported-plugins.AntiRelog.prevent-tag-in-duel", true);
        pmPreventDuel = configuration.getBoolean("supported-plugins.PvPManager.prevent-duel-if-tagged", true);
        pmPreventTag = configuration.getBoolean("supported-plugins.PvPManager.prevent-tag-in-duel", true);
        clxPreventDuel = configuration.getBoolean("supported-plugins.CombatLogX.prevent-duel-if-tagged", true);
        clxPreventTag = configuration.getBoolean("supported-plugins.CombatLogX.prevent-tag-in-duel", true);
        autoUnvanish = configuration.getBoolean("supported-plugins.Essentials.auto-unvanish", true);
        setBackLocation = configuration.getBoolean("supported-plugins.Essentials.set-back-location", true);
        disableSkills = configuration.getBoolean("supported-plugins.mcMMO.disable-skills-in-duel", true);
        fuNoPowerLoss = configuration.getBoolean("supported-plugins.FactionsUUID.no-power-loss-in-duel", true);
        fNoPowerLoss = configuration.getBoolean("supported-plugins.Factions.no-power-loss-in-duel", true);
        duelzoneEnabled = configuration.getBoolean("supported-plugins.WorldGuard.duelzone.enabled", false);
        duelzones = configuration.getStringList("supported-plugins.WorldGuard.duelzone.regions");
        myPetDespawn = configuration.getBoolean("supported-plugins.MyPet.despawn-pet-in-duel", false);
        preventBountyLoss = configuration.getBoolean("supported-plugins.BountyHunters.prevent-bounty-loss-in-duel", true);
        preventKDRChange = configuration.getBoolean("supported-plugins.SimpleClans.prevent-kdr-change", true);
        lhWinsCmd = configuration.getString("supported-plugins.LeaderHeads.wins.menu.command", "openwins");
        lhWinsTitle = configuration.getString("supported-plugins.LeaderHeads.wins.menu.title", "Duel Wins");
        lhLossesCmd = configuration.getString("supported-plugins.LeaderHeads.losses.menu.command", "openlosses");
        lhLossesTitle = configuration.getString("supported-plugins.LeaderHeads.losses.menu.title", "Duel Losses");

        requiresClearedInventory = configuration.getBoolean("request.requires-cleared-inventory", true);
        preventCreativeMode = configuration.getBoolean("request.prevent-creative-mode", false);
        ownInventoryEnabled = configuration.getBoolean("request.use-own-inventory.enabled", true);
        ownInventoryDropInventoryItems = configuration.getBoolean("request.use-own-inventory.drop-inventory-items", false);
        ownInventoryUsePermission = configuration.getBoolean("request.use-own-inventory.use-permission", false);
        kitSelectingEnabled = configuration.getBoolean("request.kit-selecting.enabled", true);
        kitSelectingUsePermission = configuration.getBoolean("request.kit-selecting.use-permission", false);
        arenaSelectingEnabled = configuration.getBoolean("request.arena-selecting.enabled", true);
        arenaSelectingUsePermission = configuration.getBoolean("request.arena-selecting.use-permission", false);
        itemBettingEnabled = configuration.getBoolean("request.item-betting.enabled", true);
        itemBettingUsePermission = configuration.getBoolean("request.item-betting.use-permission", false);
        moneyBettingEnabled = configuration.getBoolean("request.money-betting.enabled", true);
        moneyBettingUsePermission = configuration.getBoolean("request.money-betting.use-permission", false);
        expiration = Math.max(configuration.getInt("request.expiration", 30), 0);

        maxDuration = configuration.getInt("duel.match.max-duration", -1);
        minY = configuration.getInt("duel.match.min-y", -100);
        startCommandsEnabled = configuration.getBoolean("duel.match.start-commands.enabled", false);
        startCommandsQueueOnly = configuration.getBoolean("duel.match.start-commands.queue-matches-only", false);
        startCommands = configuration.getStringList("duel.match.start-commands.commands");
        endCommandsEnabled = configuration.getBoolean("duel.match.end-commands.enabled", false);
        endCommandsQueueOnly = configuration.getBoolean("duel.match.end-commands.queue-matches-only", false);
        endCommands = configuration.getStringList("duel.match.end-commands.commands");
        tieCommandsEnabled = configuration.getBoolean("duel.match.tie-commands.enabled", false);
        tieCommandsQueueOnly = configuration.getBoolean("duel.match.tie-commands.queue-matches-only", false);
        tieCommands = configuration.getStringList("duel.match.tie-commands.commands");
        projectileHitMessageEnabled = configuration.getBoolean("duel.projectile-hit-message.enabled", true);
        projectileHitMessageTypes = configuration.getStringList("duel.projectile-hit-message.types");
        preventInventoryOpen = configuration.getBoolean("duel.prevent-inventory-open", true);
        protectKitItems = configuration.getBoolean("duel.protect-kit-items", true);
        removeEmptyBottle = configuration.getBoolean("duel.remove-empty-bottle", true);
        preventTpToMatchPlayers = configuration.getBoolean("duel.prevent-teleport-to-match-players", true);
        forceAllowCombat = configuration.getBoolean("duel.force-allow-combat", true);
        cancelIfMoved = configuration.getBoolean("duel.cancel-if-moved", false);
        blacklistedWorlds = configuration.getStringList("duel.blacklisted-worlds");
        teleportToLastLocation = configuration.getBoolean("duel.teleport-to-last-location", false);
        teleportDelay = configuration.getInt("duel.teleport-delay", 5);
        spawnFirework = configuration.getBoolean("duel.spawn-firework", true);
        arenaOnlyEndMessage = configuration.getBoolean("duel.arena-only-end-message", false);
        displayInventories = configuration.getBoolean("duel.display-inventories", true);
        preventItemDrop = configuration.getBoolean("duel.prevent-item-drop", false);
        clearItemsAfterMatch = configuration.getBoolean("duel.clear-items-after-duel", false);
        preventItemPickup = configuration.getBoolean("duel.prevent-item-pickup", true);
        limitTeleportEnabled = configuration.getBoolean("duel.limit-teleportation.enabled", true);
        distanceAllowed = configuration.getDouble("duel.limit-teleportation.distance-allowed", 5.0);
        blockAllCommands = configuration.getBoolean("duel.block-all-commands", false);
        whitelistedCommands = configuration.getStringList("duel.whitelisted-commands");
        blacklistedCommands = configuration.getStringList("duel.blacklisted-commands");

        queueBlacklistedCommands = configuration.getStringList("queue.blacklisted-commands");

        ratingEnabled = configuration.getBoolean("rating.enabled", true);
        kFactor = Math.max(configuration.getInt("rating.k-factor", 32), 1);
        defaultRating = Math.max(configuration.getInt("rating.default-rating", 1400), 0);
        ratingQueueOnly = configuration.getBoolean("rating.queue-matches-only", true);

        specRequiresClearedInventory = configuration.getBoolean("spectate.requires-cleared-inventory", false);
        specUseSpectatorGamemode = configuration.getBoolean("spectate.use-spectator-gamemode", false);
        specAddInvisibilityEffect = configuration.getBoolean("spectate.add-invisibility-effect", true);
        specWhitelistedCommands = configuration.getStringList("spectate.whitelisted-commands");

        cdEnabled = configuration.getBoolean("countdown.enabled", true);
        cdMessages = configuration.getStringList("countdown.messages");
        titles = configuration.getStringList("countdown.titles");
        preventMovement = configuration.getBoolean("countdown.prevent.movement", true);
        preventLaunchProjectile = configuration.getBoolean("countdown.prevent.launch-projectile", true);
        preventPvp = configuration.getBoolean("countdown.prevent.pvp", true);
        preventInteract = configuration.getBoolean("countdown.prevent.interact", true);

        displayKitRatings = configuration.getBoolean("stats.display-kit-ratings", true);
        displayNoKitRating = configuration.getBoolean("stats.display-nokit-rating", false);
        displayPastMatches = configuration.getBoolean("stats.display-past-matches", true);
        matchesToDisplay = Math.max(configuration.getInt("stats.matches-to-display", 10), 0);

        topUpdateInterval = Math.max(configuration.getInt("top.update-interval", 5), 1) * 60L * 1000L;
        topWinsType = configuration.getString("top.displayed-replacers.wins.type", "Wins");
        topWinsIdentifier = configuration.getString("top.displayed-replacers.wins.identifier", "wins");
        topLossesType = configuration.getString("top.displayed-replacers.losses.type", "Losses");
        topLossesIdentifier = configuration.getString("top.displayed-replacers.losses.identifier", "losses");
        topKitType = configuration.getString("top.displayed-replacers.kit.type", "%kit%");
        topKitIdentifier = configuration.getString("top.displayed-replacers.kit.identifier", "rating");
        topNoKitType = configuration.getString("top.displayed-replacers.no-kit.type", "No Kit");
        topNoKitIdentifier = configuration.getString("top.displayed-replacers.no-kit.identifier", "rating");

        kitSelectorRows = Math.min(Math.max(configuration.getInt("guis.kit-selector.rows", 2), 1), 5);
        kitSelectorFillerType = configuration.getString("guis.kit-selector.space-filler-item.type", "STAINED_GLASS_PANE");
        kitSelectorFillerData = (short) configuration.getInt("guis.kit-selector.space-filler-item.data", 0);
        arenaSelectorRows = Math.min(Math.max(configuration.getInt("guis.arena-selector.rows", 3), 1), 5);
        arenaSelectorFillerType = configuration.getString("guis.arena-selector.space-filler-item.type", "STAINED_GLASS_PANE");
        arenaSelectorFillerData = (short) configuration.getInt("guis.arena-selector.space-filler-item.data", 0);
        settingsFillerType = configuration.getString("guis.settings.space-filler-item.type", "STAINED_GLASS_PANE");
        settingsFillerData = (short) configuration.getInt("guis.settings.space-filler-item.data", 0);
        queuesRows = Math.min(Math.max(configuration.getInt("guis.queues.rows", 3), 1), 5);
        queuesFillerType = configuration.getString("guis.queues.space-filler-item.type", "STAINED_GLASS_PANE");
        queuesFillerData = (short) configuration.getInt("guis.queues.space-filler-item.data", 0);
        inheritKitItemType = configuration.getBoolean("guis.queues.inherit-kit-item-type", true);

        soupHeartsToRegen = Math.max(configuration.getDouble("soup.hearts-to-regen", 3.5), 0);
        soupRemoveEmptyBowl = configuration.getBoolean("soup.remove-empty-bowl", true);
        soupCancelIfAlreadyFull = configuration.getBoolean("soup.cancel-if-already-full", true);

        final ConfigurationSection sounds = configuration.getConfigurationSection("sounds");

        if (sounds != null) {
            for (final String name : sounds.getKeys(false)) {
                final ConfigurationSection sound = sounds.getConfigurationSection(name);
                final Sound type = EnumUtil.getByName(sound.getString("type"), Sound.class);

                if (type == null) {
                    continue;
                }

                this.sounds.put(name, new MessageSound(type, sound.getDouble("pitch"), sound.getDouble("volume"), sound.getStringList("trigger-messages")));
            }
        }
    }

    public void playSound(final Player player, final String message) {
        sounds.values().stream()
            .filter(sound -> sound.getMessages().contains(message))
            .forEach(sound -> player.playSound(player.getLocation(), sound.getType(), sound.getVolume(), sound.getPitch()));
    }

    public MessageSound getSound(final String name) {
        return sounds.get(name);
    }

    public Set<String> getSounds() {
        return sounds.keySet();
    }

    public class MessageSound {

        @Getter
        private final Sound type;
        @Getter
        private final float pitch, volume;
        @Getter
        private final List<String> messages;

        MessageSound(final Sound type, final double pitch, final double volume, final List<String> messages) {
            this.type = type;
            this.pitch = (float) pitch;
            this.volume = (float) volume;
            this.messages = messages;
        }
    }
}
