package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import com.alessiodp.parties.api.interfaces.Party;
import me.realized.duels.util.function.Pair;
import me.realized.duels.validator.BaseTriValidator;

public class TargetNoRequestValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {
    
    private static final String MESSAGE_KEY = "ERROR.duel.no-request";
    private static final String PARTY_MESSAGE_KEY = "ERROR.party-duel.no-request";

    public TargetNoRequestValidator(DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(Pair<Player, Player> pair, Party party, Collection<Player> players) {
        if (!requestManager.has(pair.getValue(), pair.getKey())) {
            lang.sendMessage(pair.getKey(), party != null ? PARTY_MESSAGE_KEY : MESSAGE_KEY, "name", pair.getValue().getName());
            return false;
        }

        return true;
    }
}
