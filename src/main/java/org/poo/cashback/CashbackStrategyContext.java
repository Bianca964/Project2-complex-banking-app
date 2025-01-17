package org.poo.cashback;

import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.transactions.Commerciant;
import org.poo.users.User;

@Setter
@Getter
public class CashbackStrategyContext {
    CashbackStrategy cashbackStrategy;

    public CashbackStrategyContext() {
        this.cashbackStrategy = null;
    }

    public void setCashbackStrategy(Commerciant commerciant) {
        if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            this.cashbackStrategy = new NrOfTransactionsCashback();
        } else if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
            this.cashbackStrategy = new SpendingThresholdCashback();
        }
    }

    public void applyCashback(Commerciant commerciant, User sender, Account senderAccount,
                              double amountInAccountCurrency, double amountInRon) {
        if (cashbackStrategy == null) {
            return;
        }
        cashbackStrategy.applyCashback(commerciant, sender, senderAccount, amountInAccountCurrency, amountInRon);
    }
}
