package org.poo.cashback;

import org.poo.accounts.Account;
import org.poo.commerciants.Commerciant;
import org.poo.users.User;

public final class SpendingThresholdCashback implements CashbackStrategy {

    @Override
    public void applyCashback(final Commerciant commerciant, final  User sender,
                              final  Account senderAccount,
                              final double amountInAccountCurrency, final double amountInRon) {

        // if account doesn't have the commerciant, add it to account
        if (!senderAccount.hasCommerciant(commerciant)) {
            senderAccount.addCommerciant(commerciant);
        }

        // if there is a discount available, apply it and then add another if possible
        if (senderAccount.hasDiscountAvailable()) {
            senderAccount.applyDiscount(commerciant, amountInAccountCurrency);
        }

        senderAccount.addAmountForSpendingThreshold(amountInRon);
        senderAccount.applySpendingThresholdDiscount(sender, amountInAccountCurrency);
    }
}
