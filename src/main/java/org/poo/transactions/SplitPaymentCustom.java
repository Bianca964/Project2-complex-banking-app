package org.poo.transactions;

import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.users.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

public class SplitPaymentCustom extends SplitPayment {
    private ArrayList<Double> amountsForUsers;

    public SplitPaymentCustom(final Bank bank, final CommandInput commandInput) {
        super(bank, commandInput);

        this.amountsForUsers = new ArrayList<>();
        for (double amount : commandInput.getAmountForUsers()) {
            this.amountsForUsers.add(amount);
        }
    }


//    public boolean isCustomType() {
//        return true;
//    }
//
//    public boolean isEqualType() {
//        return false;
//    }

    public String getType() {
        return "custom";
    }



    public Account everyoneHasEnoughBalance(final List<Account> accounts, final List<Double> amounts,
                                            final String currency) {
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            double amount = amounts.get(i);

            double amountInAccountCurrency = 0;
            try {
                double exchangeRate = bank.getExchangeRate(currency, account.getCurrency());
                amountInAccountCurrency = amount * exchangeRate;
            } catch (Exception e) {
                return account;
            }
            if (!account.hasEnoughBalance(amountInAccountCurrency)) {
                return account;
            }
        }
        return null;
    }




    @Override
    public void execute() {

        List<String> accountsIban = new ArrayList<>();
        for (Account account : accounts) {
            accountsIban.add(account.getIban());
        }

        Account brokenAccount = everyoneHasEnoughBalance(accounts, amountsForUsers, currency);
        // if not everyone has enough money (if brokenAccount is null, everyone has enough money)
        if (brokenAccount != null) {
            for (Account account : accounts) {
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setDescription("Split payment of " + String.format("%.2f", totalAmount)
                                + " " + currency)
                        .setError("Account " + brokenAccount.getIban()
                                + " has insufficient funds for a split payment.")
                        .setTimestamp(timestamp)
                        .setCurrency(currency)
                        .setAmountsSplitTransaction(amountsForUsers)
                        .setInvolvedAccounts(accountsIban)
                        .setSplitPaymentType("custom")
                        .build();

                User user = bank.getUserWithAccount(account.getIban());
                if (user == null) {
                    return;
                }
                user.addTransaction(transaction);
                account.addTransaction(transaction);

                // sterg splitPaymentul din lista tuturor userilor
                removeSplitPaymentFromAllInvolvedUsers();

            }
            return;
        }

        // everyone has enough balance, do the transactions
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            double amountToPay = amountsForUsers.get(i);

            double amountInAccountCurrency;
            try {
                double exchangeRate = bank.getExchangeRate(currency,
                        account.getCurrency());
                amountInAccountCurrency = amountToPay * exchangeRate;
            } catch (Exception e) {
                // if no exchange rate is found, don't do the transaction
                return;
            }

            account.withdraw(amountInAccountCurrency);

            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("Split payment of " + String.format("%.2f", totalAmount)
                            + " " + currency)
                    .setTimestamp(timestamp)
                    .setCurrency(currency)
                    .setSplitPaymentType("custom")
                    .setAmountsSplitTransaction(amountsForUsers)
                    .setInvolvedAccounts(accountsIban)
                    .build();

            User user = bank.getUserWithAccount(account.getIban());
            if (user == null) {
                return;
            }
            user.addTransaction(transaction);
            account.addTransaction(transaction);
        }

        // sterg splitPaymentul din lista tuturor userilor
        removeSplitPaymentFromAllInvolvedUsers();


    }





    public Transaction createTransactionForReject() {
        List<String> involvedAccounts = new ArrayList<>();
        for (Account account : accounts) {
            involvedAccounts.add(account.getIban());
        }

        List<Double> amounts = new ArrayList<>();
        for (double amount : amountsForUsers) {
            amounts.add(amount);
        }

        Transaction transaction = new Transaction.TransactionBuilder()
                .setTimestamp(timestamp)
                .setError("One user rejected the payment.")
                .setDescription("Split payment of " + String.format("%.2f", totalAmount)
                        + " " + currency)
                .setCurrency(currency)
                .setSplitPaymentType("custom")
                .setInvolvedAccounts(involvedAccounts)
                .setAmountsSplitTransaction(amounts)
                .build();

        return transaction;
    }


}
