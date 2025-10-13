package me.realized.duels.player;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import me.realized.duels.util.PlayerUtil;
import me.realized.duels.util.inventory.InventoryUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerInfo {

    @Getter
    private final Map<String, Map<Integer, ItemStack>> items = new HashMap<>();
    @Getter
    private final List<PotionEffect> effects;
    @Getter
    private final double health;
    @Getter
    private final int hunger;
    @Getter
    private final List<ItemStack> extra = new ArrayList<>();
    @Getter
    @Setter
    private Location location;

    public PlayerInfo(final List<PotionEffect> effects, final double health, final int hunger, final Location location) {
        this.effects = effects;
        this.health = health;
        this.hunger = hunger;
        this.location = location;
    }

    public PlayerInfo(final Player player, final boolean excludeInventory) {
        this(Lists.newArrayList(player.getActivePotionEffects()), player.getHealth(), player.getFoodLevel(), player.getLocation().clone());

        if (excludeInventory) {
            return;
        }

        InventoryUtil.addToMap(player.getInventory(), items);
    }

    public void restore(final Player player) {
        final double maxHealth = PlayerUtil.getMaxHealth(player);
        player.addPotionEffects(effects);
        player.setHealth(health > maxHealth ? maxHealth : health);
        player.setFoodLevel(hunger);
        InventoryUtil.fillFromMap(player.getInventory(), items);
        InventoryUtil.addOrDrop(player, extra);
        player.updateInventory();
    }
}
