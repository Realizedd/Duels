package me.realized.duels.validator.validators.request.self;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import com.alessiodp.parties.api.interfaces.Party;
import me.realized.duels.validator.BaseTriValidator;

public class SelfCheckSpectateValidator extends BaseTriValidator<Player, Party, Collection<Player>> {
    
    private static final String MESSAGE_KEY = "ERROR.duel.already-spectating.sender";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.already-spectating.sender";

    public SelfCheckSpectateValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        if (players.stream().anyMatch(player -> spectateManager.isSpectating(player))) {
            lang.sendMessage(sender, party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY);
            return false;
        }

        return true;
    }
}
