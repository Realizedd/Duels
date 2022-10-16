package me.realized.duels.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.alessiodp.parties.api.interfaces.Party;
import com.google.common.collect.ImmutableList;

import org.bukkit.entity.Player;

import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.setting.Settings;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.function.Pair;
import me.realized.duels.util.validator.BiValidator;
import me.realized.duels.util.validator.TriValidator;
import me.realized.duels.util.validator.ValidatorUtil;
import me.realized.duels.validator.validators.match.BlacklistedWorldValidator;
import me.realized.duels.validator.validators.match.CheckAliveValidator;
import me.realized.duels.validator.validators.match.CheckMoveValidator;
import me.realized.duels.validator.validators.match.CombatTagValidator;
import me.realized.duels.validator.validators.match.DuelZoneValidator;
import me.realized.duels.validator.validators.match.ModeValidator;
import me.realized.duels.validator.validators.match.PartyValidator;
import me.realized.duels.validator.validators.match.PreventCreativeValidator;
import me.realized.duels.validator.validators.request.self.SelfBlacklistedWorldValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckMatchValidator;
import me.realized.duels.validator.validators.request.self.SelfCheckSpectateValidator;
import me.realized.duels.validator.validators.request.self.SelfCombatTagValidator;
import me.realized.duels.validator.validators.request.self.SelfDuelZoneValidator;
import me.realized.duels.validator.validators.request.self.SelfEmptyInventoryValidator;
import me.realized.duels.validator.validators.request.self.SelfPreventCreativeValidator;
import me.realized.duels.validator.validators.request.target.TargetCanRequestValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckMatchValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckSelfValidator;
import me.realized.duels.validator.validators.request.target.TargetCheckSpectateValidator;
import me.realized.duels.validator.validators.request.target.TargetHasRequestValidator;
import me.realized.duels.validator.validators.request.target.TargetNoRequestValidator;
import me.realized.duels.validator.validators.request.target.TargetPartyValidator;

public class ValidatorManager implements Loadable {

    private final Map<Class<?>, TriValidator<Player, Party, Collection<Player>>> selfValidators = new HashMap<>();
    private final Map<Class<?>, TriValidator<Pair<Player, Player>, Party, Collection<Player>>> targetValidators = new HashMap<>();

    private final DuelsPlugin plugin;
    
    @Getter
    private ImmutableList<TriValidator<Player, Party, Collection<Player>>> duelSelfValidators;
    @Getter
    private ImmutableList<TriValidator<Pair<Player, Player>, Party, Collection<Player>>> duelTargetValidators;

    @Getter
    private ImmutableList<TriValidator<Player, Party, Collection<Player>>> duelAcceptSelfValidators;
    @Getter
    private ImmutableList<TriValidator<Pair<Player, Player>, Party, Collection<Player>>> duelAcceptTargetValidators;
    
    @Getter
    private ImmutableList<TriValidator<Pair<Player, Player>, Party, Collection<Player>>> duelDenyTargetValidators;

    @Getter
    private ImmutableList<BiValidator<Collection<Player>, Settings>> matchValidators;

    public ValidatorManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {
        selfValidators.put(SelfEmptyInventoryValidator.class, new SelfEmptyInventoryValidator(plugin));
        selfValidators.put(SelfPreventCreativeValidator.class, new SelfPreventCreativeValidator(plugin));
        selfValidators.put(SelfBlacklistedWorldValidator.class, new SelfEmptyInventoryValidator(plugin));
        selfValidators.put(SelfCombatTagValidator.class, new SelfPreventCreativeValidator(plugin));
        selfValidators.put(SelfDuelZoneValidator.class, new SelfEmptyInventoryValidator(plugin));
        selfValidators.put(SelfCheckMatchValidator.class, new SelfPreventCreativeValidator(plugin));
        selfValidators.put(SelfCheckSpectateValidator.class, new SelfEmptyInventoryValidator(plugin));
        
        targetValidators.put(TargetCheckSelfValidator.class, new TargetCheckSelfValidator(plugin));
        targetValidators.put(TargetCanRequestValidator.class, new TargetCanRequestValidator(plugin));
        targetValidators.put(TargetPartyValidator.class, new TargetPartyValidator(plugin));
        targetValidators.put(TargetCheckMatchValidator.class, new TargetCheckMatchValidator(plugin));
        targetValidators.put(TargetCheckSpectateValidator.class, new TargetCheckSpectateValidator(plugin));
        targetValidators.put(TargetNoRequestValidator.class, new TargetNoRequestValidator(plugin));
        targetValidators.put(TargetHasRequestValidator.class, new TargetHasRequestValidator(plugin));

        duelSelfValidators = ValidatorUtil.buildList(
            self(SelfEmptyInventoryValidator.class),
            self(SelfPreventCreativeValidator.class),
            self(SelfBlacklistedWorldValidator.class),
            self(SelfCombatTagValidator.class),
            self(SelfDuelZoneValidator.class),
            self(SelfCheckMatchValidator.class),
            self(SelfCheckSpectateValidator.class)
        );
        duelTargetValidators = ValidatorUtil.buildList(
            target(TargetCheckSelfValidator.class),
            target(TargetCanRequestValidator.class),
            target(TargetPartyValidator.class),
            target(TargetCheckMatchValidator.class),
            target(TargetCheckSpectateValidator.class),
            target(TargetHasRequestValidator.class)
        );

        duelAcceptSelfValidators = duelSelfValidators;
        duelAcceptTargetValidators = ValidatorUtil.buildList(
            target(TargetCheckSelfValidator.class),
            target(TargetPartyValidator.class),
            target(TargetCheckMatchValidator.class),
            target(TargetCheckSpectateValidator.class),
            target(TargetNoRequestValidator.class)
        );

        duelDenyTargetValidators = ValidatorUtil.buildList(
            target(TargetNoRequestValidator.class)
        );

        matchValidators = ValidatorUtil.buildList(
            new ModeValidator(plugin),
            new CheckAliveValidator(plugin),
            new PreventCreativeValidator(plugin),
            new BlacklistedWorldValidator(plugin),
            new CheckMoveValidator(plugin),
            new CombatTagValidator(plugin),
            new DuelZoneValidator(plugin)
//            new PartyValidator(plugin)
        );
    }

    @Override
    public void handleUnload() {
        selfValidators.clear();
        targetValidators.clear();
    }

    private TriValidator<Player, Party, Collection<Player>> self(Class<? extends TriValidator<Player, Party, Collection<Player>>> clazz) {
        return selfValidators.get(clazz);
    }

    private TriValidator<Pair<Player, Player>, Party, Collection<Player>> target(Class<? extends TriValidator<Pair<Player, Player>, Party, Collection<Player>>> clazz) {
        return targetValidators.get(clazz);
    }
}
