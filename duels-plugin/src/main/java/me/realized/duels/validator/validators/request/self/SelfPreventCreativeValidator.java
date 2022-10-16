package me.realized.duels.validator.validators.request.self;

import org.bukkit.entity.Player;

import java.util.Collection;

import org.bukkit.GameMode;

import me.realized.duels.validator.BaseTriValidator;
import me.realized.duels.DuelsPlugin;
import com.alessiodp.parties.api.interfaces.Party;

public class SelfPreventCreativeValidator extends BaseTriValidator<Player, Party, Collection<Player>> {
    
    private static final String MESSAGE_KEY = "ERROR.duel.in-creative-mode";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.in-creative-mode";

    public SelfPreventCreativeValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean shouldValidate() {
        return config.isPreventCreativeMode();
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> player.getGameMode() == GameMode.CREATIVE)) {
            lang.sendMessage(sender, party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
