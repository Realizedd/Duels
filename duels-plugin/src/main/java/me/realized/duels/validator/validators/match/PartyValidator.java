package me.realized.duels.validator.validators.match;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.realized.duels.DuelsPlugin;
import me.realized.duels.setting.Settings;
import me.realized.duels.validator.BaseBiValidator;

public class PartyValidator extends BaseBiValidator<Collection<Player>, Settings> {

    public PartyValidator(final DuelsPlugin plugin) {
        super(plugin);
    }

    @Override
    public boolean validate(final Collection<Player> players, final Settings settings) {
        if (!settings.isPartyDuel()) {
            return true;
        }

        int senderPartySize = settings.getSenderParty().getMembers().size();
        int targetPartySize = settings.getTargetParty().getMembers().size();

        if (config.isPartySameSizeOnly() && senderPartySize != targetPartySize) {
            lang.sendMessage(players, "DUEL.party-start-failure.is-not-same-size");
            return false;
        }

        if (players.size() != senderPartySize + targetPartySize) {
            lang.sendMessage(players, "DUEL.party-start-failure.is-not-all-online");
            return false;
        }

        return true;
    }
}
