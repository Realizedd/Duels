package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import com.alessiodp.parties.api.interfaces.Party;
import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.util.function.Pair;
import me.realized.duels.validator.BaseTriValidator;

public class TargetCheckSpectateValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {
    
    private static final String MESSAGE_KEY = "ERROR.duel.already-spectating.target";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.already-spectating.target";

    public TargetCheckSpectateValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(spectateManager::isSpectating)) {
            lang.sendMessage(pair.getKey(), party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
