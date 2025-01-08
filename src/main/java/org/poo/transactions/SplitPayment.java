package org.poo.transactions;

import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.users.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

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



    public SplitPayment(final Bank bank, CommandInput commandInput) {
        this.bank = bank;
        this.accepts = 0;

        ArrayList<User> users = new ArrayList<>();
        ArrayList<Account> accounts = new ArrayList<>();
        for (String accountIBAN : commandInput.getAccounts()) {
            User user = bank.getUserWithAccount(accountIBAN);
            Account account = bank.getAccountWithIBAN(accountIBAN);
            if (user != null) {
                users.add(user);
            }
            if (account != null) {
                accounts.add(account);
            }
        }

        this.users = users;
        this.accounts = accounts;
        this.totalAmount = commandInput.getAmount();
        this.currency = commandInput.getCurrency();
        this.timestamp = commandInput.getTimestamp();
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void removeUser(User user) {
        this.users.remove(user);
    }

    public void incrementAccepts() {
        accepts++;
    }

    public boolean everyoneHasAccepted() {
        return accepts == users.size();
    }

    public void removeSplitPaymentFromAllInvolvedUsers() {
        for (User user : users) {
            user.removeSplitPayment(this);
        }
    }


    public abstract String getType();
    public abstract void execute();
    public abstract Transaction createTransactionForReject();



}
