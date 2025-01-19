package org.poo.transactions.splitpayments;

import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.transactions.Transaction;
import org.poo.users.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

@Getter
@Setter
public abstract class SplitPayment {
    protected Bank bank;
    protected int accepts;
    protected ArrayList<User> users;
    protected ArrayList<Account> accounts;
    protected double totalAmount;
    protected String currency;
    protected int timestamp;

    public SplitPayment(final Bank bank, final CommandInput commandInput) {
        this.bank = bank;
        this.accepts = 0;

        ArrayList<User> usersList = new ArrayList<>();
        ArrayList<Account> accountsList = new ArrayList<>();
        for (String accountIBAN : commandInput.getAccounts()) {
            User user = bank.getUserWithAccount(accountIBAN);
            Account account = bank.getAccountWithIBAN(accountIBAN);
            if (user != null) {
                usersList.add(user);
            }
            if (account != null) {
                accountsList.add(account);
            }
        }

        this.users = usersList;
        this.accounts = accountsList;
        this.totalAmount = commandInput.getAmount();
        this.currency = commandInput.getCurrency();
        this.timestamp = commandInput.getTimestamp();
    }

    /**
     * Increments the number of users that have accepted the split payment
     */
    public void incrementAccepts() {
        accepts++;
    }

    /**
     * @return true if everyone has accepted the split payment, false otherwise
     */
    public boolean everyoneHasAccepted() {
        return accepts == users.size();
    }

    /**
     * Removes the split payment from all involved users
     */
    public void removeSplitPaymentFromAllInvolvedUsers() {
        for (User user : users) {
            user.removeSplitPayment(this);
        }
    }

    /**
     * @return the type of the split payment
     */
    public abstract String getType();

    /**
     * Executes the split payment command
     */
    public abstract void execute();

    /**
     * Creates a transaction for the split payment
     * @return the transaction for the split payment
     */
    public abstract Transaction createTransactionForReject();
}
