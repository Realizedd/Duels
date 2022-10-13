package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import com.alessiodp.parties.api.interfaces.Party;
import me.realized.duels.util.function.Pair;
import me.realized.duels.validator.BaseTriValidator;

public class TargetCheckSelfValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {

    public TargetCheckSelfValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        if (pair.getKey().equals(pair.getValue())) {
            lang.sendMessage(pair.getKey(), "ERROR.duel.is-self");
            return false;
        }

        return true;
    }

    
}