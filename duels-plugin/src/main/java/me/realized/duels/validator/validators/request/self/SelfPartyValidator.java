package me.realized.duels.validator.validators.request.self;

import java.util.Collection;
import java.util.Objects;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import com.alessiodp.parties.api.interfaces.Party;
import me.realized.duels.validator.BaseTriValidator;

public class SelfPartyValidator extends BaseTriValidator<Player, Party, Collection<Player>> {

    public SelfPartyValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Player sender, final Party party, final Collection<Player> players) {
        // Skip for 1v1s
        if (party == null) {
            return true;
        }

        if (!Objects.equals(party.getLeader(), sender.getUniqueId())) {
            lang.sendMessage(sender, "ERROR.party.is-not-owner");
            return false;
        }

        if (players.size() != party.getMembers().size()) {
            lang.sendMessage(sender, "ERROR.party.is-not-online.sender");
            return false;
        }

        return true;
    }
}
