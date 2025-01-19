package org.poo.transactions.splitpayments;

import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.transactions.Transaction;
import org.poo.users.User;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

public final class SplitPaymentCustom extends SplitPayment {
    private final ArrayList<Double> amountsForUsers;

    public SplitPaymentCustom(final Bank bank, final CommandInput commandInput) {
        super(bank, commandInput);

        this.amountsForUsers = new ArrayList<>();
        this.amountsForUsers.addAll(commandInput.getAmountForUsers());
    }

    @Override
    public String getType() {
        return "custom";
    }

    /**
     * Checks if everyone has enough balance for a split payment
     * @param accounts list of accounts involved in the split payment
     * @param amounts list of amounts each user has to pay in the given currency
     * @param currency the currency in which the amount is given
     * @return the first account that has insufficient funds or null if everyone has enough money
     */
    public Account everyoneHasEnoughBalance(final List<Account> accounts,
                                            final List<Double> amounts, final String currency) {
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

        removeSplitPaymentFromAllInvolvedUsers();
    }

    @Override
    public Transaction createTransactionForReject() {
        List<String> involvedAccounts = new ArrayList<>();
        for (Account account : accounts) {
            involvedAccounts.add(account.getIban());
        }

        List<Double> amounts = new ArrayList<>();
        for (double amount : amountsForUsers) {
            amounts.add(amount);
        }

        return new Transaction.TransactionBuilder()
                .setTimestamp(timestamp)
                .setError("One user rejected the payment.")
                .setDescription("Split payment of " + String.format("%.2f", totalAmount)
                        + " " + currency)
                .setCurrency(currency)
                .setSplitPaymentType("custom")
                .setInvolvedAccounts(involvedAccounts)
                .setAmountsSplitTransaction(amounts)
                .build();
    }


}
