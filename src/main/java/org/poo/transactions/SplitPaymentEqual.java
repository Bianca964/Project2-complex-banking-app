package org.poo.transactions;

import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.users.User;
import org.poo.fileio.CommandInput;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SplitPaymentEqual extends SplitPayment {
    private double amountSplitted;

    public SplitPaymentEqual(final Bank bank, final CommandInput commandInput) {
        super(bank, commandInput);
        this.amountSplitted = totalAmount / accounts.size();
    }

    public String getType() {
        return "equal";
    }


    /**
     * Checks if everyone has enough balance for a split payment
     * @param accounts list of accounts involved in the split payment
     * @param amount the amount of each user to pay in the given currency
     * @param currency the currency in which the amount is given
     * @return the last account that has insufficient funds or null if everyone has enough money
     */
    public Account everyoneHasEnoughBalance(final List<Account> accounts, final double amount,
                                            final String currency) {
        Account brokenAccount = null;
        for (Account account : accounts) {
            double amountInAccountCurrency = 0;
            try {
                double exchangeRate = bank.getExchangeRate(currency, account.getCurrency());
                amountInAccountCurrency = amount * exchangeRate;
            } catch (Exception e) {
                brokenAccount = account;
            }
            if (!account.hasEnoughBalance(amountInAccountCurrency)) {
                brokenAccount = account;
            }
        }
        return brokenAccount;
    }



    public double getRoundedAmountSplitted() {
        //return new BigDecimal(amountSplitted).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return amountSplitted;
    }


    /**
     * Executes the split payment command
     */
    @Override
    public void execute() {
        List<String> accountsIban = new ArrayList<>();
        for (Account account : accounts) {
            accountsIban.add(account.getIban());
        }


        // if not everyone has enough money (if brokenAccount is null, everyone has enough money)
        Account brokenAccount = everyoneHasEnoughBalance(accounts, amountSplitted, currency);
        if (brokenAccount != null) {
            for (Account account : accounts) {
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setDescription("Split payment of " + String.format("%.2f", totalAmount)
                                + " " + currency)
                        .setError("Account " + brokenAccount.getIban()
                                + " has insufficient funds for a split payment.")
                        .setTimestamp(timestamp)
                        .setCurrency(currency)
                        .setAmountSplitted(getRoundedAmountSplitted())
                        .setInvolvedAccounts(accountsIban)
                        .setSplitPaymentType("equal")
                        .build();

                User user = bank.getUserWithAccount(account.getIban());
                if (user == null) {
                    return;
                }
                user.addTransaction(transaction);
                account.addTransaction(transaction);
            }
            return;
        }

        // everyone has enough money, do the transactions
        for (Account account : accounts) {
            double amountInAccountCurrency;
            try {
                double exchangeRate = bank.getExchangeRate(currency,
                        account.getCurrency());
                amountInAccountCurrency = amountSplitted * exchangeRate;
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
                    .setSplitPaymentType("equal")
                    .setAmountSplitted(amountSplitted)
                    .setInvolvedAccounts(accountsIban)
                    .build();

            User user = bank.getUserWithAccount(account.getIban());
            if (user == null) {
                return;
            }

            user.addTransaction(transaction);
            account.addTransaction(transaction);
        }
    }



    public Transaction createTransactionForReject() {
        List<String> involvedAccounts = new ArrayList<>();
        for (Account account : accounts) {
            involvedAccounts.add(account.getIban());
        }

        List<Double> amounts = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            amounts.add(amountSplitted);
        }

        Transaction transaction = new Transaction.TransactionBuilder()
                .setTimestamp(timestamp)
                .setError("One user rejected the payment.")
                .setDescription("Split payment of " + String.format("%.2f", totalAmount)
                        + " " + currency)
                .setCurrency(currency)
                .setSplitPaymentType("equal")
                .setInvolvedAccounts(involvedAccounts)
                .setAmountSplitted(getRoundedAmountSplitted())
                .build();

        return transaction;
    }

}
