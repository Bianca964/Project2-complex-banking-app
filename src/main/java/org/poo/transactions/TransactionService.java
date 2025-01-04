package org.poo.transactions;

import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.cards.Card;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private final Bank bank;

    public TransactionService(final Bank bank) {
        this.bank = bank;
    }

    /**
     * Executes the pay online command
     * @param command the object with the whole input
     * @throws Exception if the user or the account is not found
     */
    public void payOnline(final CommandInput command) throws Exception {
        User user = bank.getUserWithEmail(command.getEmail());
        if (user == null) {
            return;
        }

        Account account = user.getAccountWithCard(command.getCardNumber());
        if (account == null) {
            throw new Exception("Card not found");
        }

        Card card = account.getCard(command.getCardNumber());
        if (card == null) {
            throw new Exception("Card not found");
        }

        // if the card is frozen, don't do the transaction
        if (card.isFrozen()) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("The card is frozen")
                    .setTimestamp(command.getTimestamp())
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
            return;
        }

        // convert amount to account currency
        double exchangeRate = bank.getExchangeRate(command.getCurrency(), account.getCurrency());
        double amountInAccountCurrency = command.getAmount() * exchangeRate;

        // comission
        double amountWithComission = user.getServicePlan().applyComission(amountInAccountCurrency, account.getCurrency());

        if (account.hasEnoughBalance(amountWithComission)) {
            account.withdraw(amountWithComission);
        } else {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("Insufficient funds")
                    .setTimestamp(command.getTimestamp())
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
            return;
        }

        card.handlePostPayment(account, user, command, amountInAccountCurrency);

        // increase the number of payments of at least 300 RON (useful for the upgradePlan case)
        double amountInRon = command.getAmount() * bank.getExchangeRate(command.getCurrency(), "RON");
        if (amountInRon >= 300) {
            user.increaseMin300payments();
        }
    }




    /**
     * Executes the transfer command
     * @param command the object with the whole input
     * @throws Exception if the user or the account is not found
     */
    public void sendMoney(final CommandInput command) throws Exception {
        Account senderAccount = bank.getAccountWithIBAN(command.getAccount());
        Account receiverAccount = bank.getAccountWithIBAN(command.getReceiver());
        User user = bank.getUserWithEmail(command.getEmail());
        User senderUser = bank.getUserWithAccount(command.getAccount());
        User receiverUser = bank.getUserWithAccount(command.getReceiver());

        if (user == null || senderUser == null || receiverUser == null) {
            return;
        }

        // account is either nonexistent or has an alias as input and the sender can't have alias
        if (senderAccount == null) {
            return;
        }

        if (receiverAccount == null) {
            // find the account by alias
            receiverAccount = bank.getAccountWithAlias(command.getReceiver());
            if (receiverAccount == null) {
                return;
            }
        }

        // convert amount to receiver currency
        double exchangeRate = bank.getExchangeRate(senderAccount.getCurrency(),
                receiverAccount.getCurrency());
        double amountInReceiverCurrency = command.getAmount() * exchangeRate;







        Transaction transactionSender = new Transaction.TransactionBuilder()
                .setDescription(command.getDescription())
                .setFromAccount(senderAccount)
                .setToAccount(receiverAccount)
                .setAmountSender(command.getAmount())
                .setAmountReceiver(amountInReceiverCurrency)
                .setTimestamp(command.getTimestamp())
                .setTransferType("sent")
                .build();

        Transaction transactionReceiver = new Transaction.TransactionBuilder()
                .setDescription(command.getDescription())
                .setFromAccount(senderAccount)
                .setToAccount(receiverAccount)
                .setAmountSender(command.getAmount())
                .setAmountReceiver(amountInReceiverCurrency)
                .setTimestamp(command.getTimestamp())
                .setTransferType("received")
                .build();

        try {
            transactionSender.doTransactionSendMoney(senderUser);
        } catch (Exception e) {
            Transaction transactionError = new Transaction.TransactionBuilder()
                    .setDescription(e.getMessage())
                    .setTimestamp(command.getTimestamp())
                    .build();

            // if the transaction failed, add it only to the sender
            user.addTransaction(transactionError);
            senderAccount.addTransaction(transactionError);
            receiverAccount.addTransaction(transactionError);
            return;
        }

        // add the transactions to the sender and receiver
        senderUser.addTransaction(transactionSender);
        receiverUser.addTransaction(transactionReceiver);
        senderAccount.addTransaction(transactionSender);
        receiverAccount.addTransaction(transactionReceiver);
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

    /**
     * Executes the split payment command
     * @param commandInput the object with the whole input
     */
    public void splitPayment(final CommandInput commandInput) {
        List<String> accountsInput = commandInput.getAccounts();
        List<Account> accounts = new ArrayList<>();
        for (String accountIBAN : accountsInput) {
            Account account = bank.getAccountWithIBAN(accountIBAN);
            if (account != null) {
                accounts.add(account);
            }
        }

        double amount = commandInput.getAmount();
        double amountToPay = amount / accounts.size();

        // if not everyone has enough money (if brokenAccount is null, everyone has enough money)
        Account brokenAccount = everyoneHasEnoughBalance(accounts, amountToPay,
                commandInput.getCurrency());
        if (brokenAccount != null) {
            for (Account account : accounts) {
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setDescription("Split payment of " + String.format("%.2f", amount)
                                + " " + commandInput.getCurrency())
                        .setError("Account " + brokenAccount.getIban()
                                + " has insufficient funds for a split payment.")
                        .setTimestamp(commandInput.getTimestamp())
                        .setCurrency(commandInput.getCurrency())
                        .setAmountSplitted(amountToPay)
                        .setInvolvedAccounts(accountsInput)
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
                double exchangeRate = bank.getExchangeRate(commandInput.getCurrency(),
                        account.getCurrency());
                amountInAccountCurrency = amountToPay * exchangeRate;
            } catch (Exception e) {
                // if no exchange rate is found, don't do the transaction
                return;
            }
            account.withdraw(amountInAccountCurrency);

            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("Split payment of " + String.format("%.2f", amount)
                            + " " + commandInput.getCurrency())
                    .setTimestamp(commandInput.getTimestamp())
                    .setCurrency(commandInput.getCurrency())
                    .setAmountSplitted(amountToPay)
                    .setInvolvedAccounts(accountsInput)
                    .build();

            User user = bank.getUserWithAccount(account.getIban());
            if (user == null) {
                return;
            }
            user.addTransaction(transaction);
            account.addTransaction(transaction);
        }
    }
}
