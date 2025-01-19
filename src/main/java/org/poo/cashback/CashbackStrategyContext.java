package org.poo.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.commerciants.Commerciant;
import org.poo.users.User;

@Setter
@Getter
public class CashbackStrategyContext {
    private CashbackStrategy cashbackStrategy;

    public CashbackStrategyContext() {
        this.cashbackStrategy = null;
    }

    /**
     * Set the cashback strategy according to the commerciant
     * @param commerciant commerciant which offers the cashback
     */
    public void setCashbackStrategy(final Commerciant commerciant) {
        if (commerciant.isNrOfTransactionType()) {
            this.cashbackStrategy = new NrOfTransactionsCashback();
        } else if (commerciant.isSpendingThresholdType()) {
            this.cashbackStrategy = new SpendingThresholdCashback();
        }
    }

    /**
     * Apply cashback according to the strategy
     * @param commerciant commerciant which offers the cashback
     * @param sender user who receives the cashback
     * @param senderAccount account of the user who receives the cashback
     * @param amountInAccountCurrency amount in the currency of the account
     * @param amountInRon amount in RON
     */
    public void applyCashback(final Commerciant commerciant, final User sender,
                              final Account senderAccount,
                              final double amountInAccountCurrency, final double amountInRon) {
        if (cashbackStrategy == null) {
            return;
        }
        cashbackStrategy.applyCashback(commerciant, sender, senderAccount,
                                       amountInAccountCurrency, amountInRon);
    }
}
