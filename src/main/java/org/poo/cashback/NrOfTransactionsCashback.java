package org.poo.cashback;

import org.poo.accounts.Account;
import org.poo.transactions.Commerciant;
import org.poo.users.User;

public class NrOfTransactionsCashback implements CashbackStrategy {

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

        if (commerciant.getNrTransactions() == 2 && !senderAccount.isDiscountFoodUsed()) {
            senderAccount.setDiscountFood();
        }
        if (commerciant.getNrTransactions() == 5 && !senderAccount.isDiscountClothesUsed()) {
            senderAccount.setDiscountClothes();
        }
        if (commerciant.getNrTransactions() == 10 && !senderAccount.isDiscountTechUsed()) {
            senderAccount.setDiscountTech();
        }
    }

}
