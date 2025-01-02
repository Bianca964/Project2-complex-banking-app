package org.poo.accounts;

import lombok.Getter;
import lombok.Setter;
import org.poo.bank.User;
import org.poo.transactions.Transaction;

@Setter
@Getter
public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final String currency, final String type, final int timestamp,
                          final double interestRate) {
        super(currency, type, timestamp);
        this.interestRate = interestRate;
    }

    /**
     * @return true as the savings account has interest
     */
    @Override
    public boolean hasInterest() {
        return true;
    }

    /**
     * adds interest to the account
     */
    public void addInterest() {
        double interest = getBalance() * interestRate;
        deposit(interest);
    }

    /**
     * @param newInterestRate the new interest rate
     */
    public void setInterestRate(final double newInterestRate, final User user,
                                final int argTimestamp) {
        this.interestRate = newInterestRate;

        Transaction transaction = new Transaction.TransactionBuilder()
                .setDescription("Interest rate of the account changed to " + newInterestRate)
                .setTimestamp(argTimestamp)
                .build();

        user.addTransaction(transaction);
        this.addTransaction(transaction);
    }

    /**
     * @return false as the savings account does not support reports
     */
    @Override
    public boolean supportsReport() {
        return false;
    }
}
