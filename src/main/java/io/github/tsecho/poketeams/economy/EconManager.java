package io.github.tsecho.poketeams.economy;

import io.github.tsecho.poketeams.PokeTeams;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;

import static io.github.tsecho.poketeams.configuration.ConfigManager.getSettings;

public class EconManager {

    private Currency currency;
    private UniqueAccount account;
    private final Cause CAUSE = Cause.of(EventContext.builder()
                                     .add(EventContextKeys.PLUGIN, PokeTeams.getInstance().getContainer())
                                     .build(), PokeTeams.getInstance().getContainer());

    public EconManager(Player player) {
        Sponge.getServiceManager().provide(EconomyService.class).ifPresent(service -> {
            currency = service.getDefaultCurrency();
            account = service.getOrCreateAccount(player.getUniqueId()).get();
        });
    }

    /**
     *
     * @return TransactionResult of the bought team (SUCCESS or NO_FUNDS)
     */
    public TransactionResult buyTeam() {
        return account.withdraw(currency, BigDecimal.valueOf(getSettings().team.money.creationCost), CAUSE);
    }

    /**
     *
     * @return TransactionResult of entering the queue
     */
    public TransactionResult enterQueue() {
        return account.withdraw(currency, BigDecimal.valueOf(getSettings().battle.queueFee.price), CAUSE);
    }

    /**
     *
     * @return TransactionResult of leaving the queue
     */
    public TransactionResult leavingQueue() {
        return account.deposit(currency, BigDecimal.valueOf(getSettings().battle.queueFee.price), CAUSE);
    }

    /**
     *
     * @param amount to withdraw from an account
     * @return TransactionResult of the withdrawn money
     */
    public TransactionResult withdraw(BigDecimal amount) {
        return account.withdraw(currency, amount, CAUSE);
    }

    /**
     *
     * @param cost to give the player
     * @return TransactionResult of the deposited money
     */
    public TransactionResult pay(BigDecimal cost) {
        return account.deposit(currency, cost, CAUSE);
    }

    /**
     *
     * @return true if economy service is present
     */
    public boolean isEnabled() {
        return currency != null;
    }
}
