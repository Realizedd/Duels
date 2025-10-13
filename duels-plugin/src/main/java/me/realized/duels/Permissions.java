package me.realized.duels;

public final class Permissions {

    public static final String DUEL = "duels.duel";
    public static final String DUEL_BOT = "duels.duel.bot";
    public static final String STATS = "duels.stats";
    public static final String STATS_OTHERS = STATS + ".others";
    public static final String TOGGLE = "duels.toggle";
    public static final String TOP = "duels.top";
    public static final String SPECTATE = "duels.spectate";
    public static final String SPEC_ANON = SPECTATE + ".anonymously";
    public static final String ADMIN = "duels.admin";
    public static final String TP_BYPASS = "duels.teleport.bypass";
    public static final String KIT = "duels.kits.%s";
    public static final String KIT_ALL = "duels.kits.*";
    public static final String KIT_SELECTING = "duels.use.kit-select";
    public static final String OWN_INVENTORY = "duels.use.own-inventory";
    public static final String ARENA_SELECTING = "duels.use.arena-select";
    public static final String ITEM_BETTING = "duels.use.item-betting";
    public static final String MONEY_BETTING = "duels.use.money-betting";
    public static final String SETTING_ALL = "duels.use.*";
    public static final String QUEUE = "duels.queue";

    private Permissions() {}
}
