package me.realized.duels.validator.validators.request.target;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.Permissions;
import me.realized.duels.data.UserData;
import com.alessiodp.parties.api.interfaces.Party;
import me.realized.duels.util.function.Pair;
import me.realized.duels.validator.BaseTriValidator;

public class TargetCanRequestValidator extends BaseTriValidator<Pair<Player, Player>, Party, Collection<Player>> {
    
    public TargetCanRequestValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Pair<Player, Player> pair, final Party party, final Collection<Player> players) {
        final UserData user = userManager.get(pair.getValue());

        if (user == null) {
            lang.sendMessage(pair.getKey(), "ERROR.data.not-found", "name", pair.getValue().getName());
            return false;
        }

        if (!pair.getKey().hasPermission(Permissions.ADMIN) && !user.canRequest()) {
            lang.sendMessage(pair.getKey(), "ERROR.duel.requests-disabled", "name",  pair.getValue().getName());
            return false;
        }

        return true;
    }

}
