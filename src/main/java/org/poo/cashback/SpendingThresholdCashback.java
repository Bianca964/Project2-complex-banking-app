package org.poo.cashback;

import org.poo.accounts.Account;
import org.poo.transactions.Commerciant;
import org.poo.users.User;

public class SpendingThresholdCashback implements CashbackStrategy {

    @Override
    public void applyCashback(Commerciant commerciant, User sender, Account senderAccount,
                              double amountInAccountCurrency, double amountInRon) {

        // if account already has the commerciant, increment the number of transactions
        if (senderAccount.getCommerciant(commerciant) != null) {
            senderAccount.incrementNrOfTrnscForCommerciant(commerciant);
        } else {
            // if account doesn't have the commerciant, add it to account
            senderAccount.addCommerciant(commerciant);
            senderAccount.getCommerciant(commerciant).setNrTransactions(1);
        }

        // if there is a discount available, apply it and then add another if possible
        if (senderAccount.hasDiscountAvailable()) {
            senderAccount.applyDiscount(commerciant, amountInAccountCurrency);
        }

        senderAccount.addAmountForSpendingThreshold(amountInRon);
        senderAccount.applySpendingThresholdDiscount(sender, amountInAccountCurrency);
    }
}
