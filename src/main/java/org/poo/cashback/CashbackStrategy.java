package org.poo.cashback;

import org.poo.accounts.Account;
import org.poo.commerciants.Commerciant;
import org.poo.users.User;

public interface CashbackStrategy {

    /**
     * Apply cashback according to the strategy
     * @param commerciant commerciant which offers the cashback
     * @param sender user who receives the cashback
     * @param senderAccount account of the user who receives the cashback
     * @param amountInAccountCurrency amount in the currency of the account
     * @param amountInRon amount in RON
     */
    void applyCashback(Commerciant commerciant, User sender, Account senderAccount,
                       double amountInAccountCurrency, double amountInRon);
}
