package org.poo.accounts;

import lombok.Getter;
import lombok.Setter;
import org.poo.users.User;
import org.poo.transactions.Transaction;

@Setter
@Getter
public final class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(final String currency, final String type, final int timestamp,
                          final double interestRate) {
        super(currency, type, timestamp);
        this.interestRate = interestRate;
    }

    /**
     * adds interest to the account
     * @return the interest added to the account
     */
    public double addInterest() {
        double interest = getBalance() * interestRate;
        deposit(interest);
        return interest;
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

    @Override
    public boolean isBusinessAccount() {
        return false;
    }

    @Override
    public boolean isSavingAccount() {
        return true;
    }

    @Override
    public boolean isClassicAccount() {
        return false;
    }
}
