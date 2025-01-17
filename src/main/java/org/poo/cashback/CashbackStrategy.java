package org.poo.cashback;

import org.poo.accounts.Account;
import org.poo.transactions.Commerciant;
import org.poo.users.User;

public interface CashbackStrategy {
    void applyCashback(Commerciant commerciant, User sender, Account senderAccount,
                       double amountInAccountCurrency, double amountInRon);
}
