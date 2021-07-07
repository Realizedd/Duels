package me.realized.duels.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PlayerInfo {

    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final Collection<PotionEffect> effects;
    private final double health;
    private final int hunger;

    @Getter
    private final List<ItemStack> extra = new ArrayList<>();

    @Getter
    @Setter
    private Location location;

    @Getter
    @Setter
    private boolean giveOnLogin;

    public PlayerInfo(final Player player, final boolean inventory) {
        this.inventory = inventory ? player.getInventory().getContents().clone() : new ItemStack[0];
        this.armor = inventory ? player.getInventory().getArmorContents().clone() : new ItemStack[0];
        this.effects = player.getActivePotionEffects();
        this.health = player.getHealth();
        this.hunger = player.getFoodLevel();
        this.location = player.getLocation().clone();
    }

    public void restore(final Player player) {
        player.addPotionEffects(effects);
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        player.setHealth(Math.min(health, maxHealth));
        player.setFoodLevel(hunger);

        if (armor.length > 0) {
            player.getInventory().setArmorContents(armor);
        }

        if (inventory.length > 0) {
            player.getInventory().setContents(inventory);
        }

        extra.stream().filter(Objects::nonNull).forEach(item -> player.getInventory().addItem(item));
        player.updateInventory();
    }
}
