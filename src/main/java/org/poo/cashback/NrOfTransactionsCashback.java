package org.poo.cashback;

import org.poo.accounts.Account;
import org.poo.commerciants.Commerciant;
import org.poo.users.User;

public final class NrOfTransactionsCashback implements CashbackStrategy {
    private static final int NR_TRANSACTIONS_FOOD = 2;
    private static final int NR_TRANSACTIONS_CLOTHES = 5;
    private static final int NR_TRANSACTIONS_TECH = 10;

    @Override
    public void applyCashback(final Commerciant commerciant, final User sender,
                              final Account senderAccount,
                              final double amountInAccountCurrency, final double amountInRon) {

        // if account already has the commerciant, increment the number of transactions
        if (senderAccount.hasCommerciant(commerciant)) {
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

        if (commerciant.getNrTransactions() == NR_TRANSACTIONS_FOOD
                && !senderAccount.isDiscountFoodUsed()) {
            senderAccount.setDiscountFood();
        }
        if (commerciant.getNrTransactions() == NR_TRANSACTIONS_CLOTHES
                && !senderAccount.isDiscountClothesUsed()) {
            senderAccount.setDiscountClothes();
        }
        if (commerciant.getNrTransactions() == NR_TRANSACTIONS_TECH
                && !senderAccount.isDiscountTechUsed()) {
            senderAccount.setDiscountTech();
        }
    }
}
