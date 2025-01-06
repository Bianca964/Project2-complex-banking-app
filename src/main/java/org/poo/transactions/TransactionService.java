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

        if (command.getAmount() <= 0) {
            return;
        }

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

        Commerciant commerciant = bank.getCommerciantWithName(command.getCommerciant());
        if (commerciant == null) {
            throw new Exception("Commerciant not found");
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


        // CASHBACK
        applyCashBack(commerciant, user, account, amountInAccountCurrency, amountInRon);
    }


    /**
     *
     * @param commerciant to which the payment was made
     * @param sender the one who made the payment
     * @param senderAccount the account from which the payment was made (which receives the cashback)
     * @param amountInAccountCurrency the amount of the payment in the sender's account currency
     * @param amountInRon the amount of the payment in RON
     */
    public void applyCashBack(Commerciant commerciant, User sender, Account senderAccount, double amountInAccountCurrency, double amountInRon) {

        // if user already has the commerciant, increment the number of transactions
        if (sender.getCommerciant(commerciant) != null) {
            sender.incrementNrOfTrnscForCommerciant(commerciant);
        } else {
            // if user doesn't have the commerciant, add it to user
            sender.addCommerciant(commerciant);
            sender.getCommerciant(commerciant).setNrTransactions(1);
        }

        if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            // if there is a discount available, apply it and then add another if possible
            if (sender.hasDiscountAvailable()) {
                sender.applyDiscount(senderAccount, commerciant, amountInAccountCurrency);
            }

            if (commerciant.getNrTransactions() == 2 && !sender.isDiscountFoodUsed()) {
                sender.setDiscountFood();
            }
            if (commerciant.getNrTransactions() == 5 && !sender.isDiscountClothesUsed()) {
                sender.setDiscountClothes();
            }
            if (commerciant.getNrTransactions() == 10 && !sender.isDiscountTechUsed()) {
                sender.setDiscountTech();
            }
        }

        if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
            // if there is a discount available, apply it and then add another if possible
            if (sender.hasDiscountAvailable()) {
                sender.applyDiscount(senderAccount, commerciant, amountInAccountCurrency);
            }

            sender.addAmountForSpendingThreshold(amountInRon);
            sender.applySpendingThresholdDiscount(senderAccount, amountInAccountCurrency);
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

        if (user == null || senderUser == null || command.getReceiver().isEmpty()) {
            throw new Exception("User not found");
        }

        // account is either nonexistent or has an alias as input and the sender can't have alias
        if (senderAccount == null) {
            return;
        }



        // The receiver is a commerciant (the IBAN belongs to a commerciant)
        Commerciant receiverCommerciant;
        if (receiverUser == null) {
            receiverCommerciant = bank.getCommerciantWithIban(command.getReceiver());
            if (receiverCommerciant != null) {
                sendMoneyToCommerciant(senderUser, senderAccount, receiverCommerciant, command);
            }
            return;
        }



        // if the receiver is not a commerciant (receiverUser != null), find the account by IBAN
        if (receiverAccount == null) {
            // find the account by alias
            receiverAccount = bank.getAccountWithAlias(command.getReceiver());
            if (receiverAccount == null) {
                return;
            }
        }

        // start the transaction to another user
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






    public void sendMoneyToCommerciant(User senderUser, Account senderAccount, Commerciant receiverCommerciant, CommandInput command) {
        // withdraw the amount from the sender's account
        double amountSender = command.getAmount();
        double amountSenderWithComission = senderUser.getServicePlan().applyComission(amountSender, senderAccount.getCurrency());

        if (senderAccount.hasEnoughBalance(amountSenderWithComission)) {
            senderAccount.withdraw(amountSenderWithComission);
        } else {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("Insufficient funds")
                    .setTimestamp(command.getTimestamp())
                    .build();

            senderUser.addTransaction(transaction);
            senderAccount.addTransaction(transaction);
            return;
        }


        // acum teoretic ar trebui sa adaug tranzactiile reusite la sender si receiver(commerciant)

        // cashback
        double exchangeRate;
        try {
            exchangeRate = bank.getExchangeRate(senderAccount.getCurrency(), "RON");
        } catch (Exception e) {
            return;
        }
        double amountInRon = amountSender * exchangeRate;

        applyCashBack(receiverCommerciant, senderUser, senderAccount, amountSender, amountInRon);
    }









    public void cashWithdrawal(CommandInput commandInput, Bank bank) throws Exception {
        Account account = bank.getAccountWithCard(commandInput.getCardNumber());
        if (account == null) {
            throw new Exception("Card not found");
        }

        Card card = account.getCard(commandInput.getCardNumber());
        if (card == null) {
            throw new Exception("Card not found");
        }

        User user = bank.getUserWithEmail(commandInput.getEmail());
        if (user == null) {
            throw new Exception("User not found");
        }

        // if the card is frozen, don't do the transaction
        if (card.isFrozen()) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("The card is frozen")
                    .setTimestamp(commandInput.getTimestamp())
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
            return;
        }

        // CAND E EROAREA: Card has already been used ????????????????????????????????

        double amountInRon = commandInput.getAmount();
        double amountInAccountCurrency = amountInRon * bank.getExchangeRate("RON", account.getCurrency());
        double amountWithComission = user.getServicePlan().applyComission(amountInAccountCurrency, account.getCurrency());

        if (!account.hasEnoughBalance(amountWithComission)) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("Insufficient funds")
                    .setTimestamp(commandInput.getTimestamp())
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
            return;
        }

        if (account.getBalance() - amountWithComission < account.getMinBalance()) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("Cannot perform payment due to a minimum balance being set")
                    .setTimestamp(commandInput.getTimestamp())
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
            return;
        }

        // withdraw cash from the account
        account.withdraw(amountWithComission);
        Transaction transaction = new Transaction.TransactionBuilder()
                //.setDescription("Cash withdrawal of " + amountInAccountCurrency)
                .setDescription("Cash withdrawal of " + commandInput.getAmount())
                .setTimestamp(commandInput.getTimestamp())
                .setAmountCashWithdrawal(amountInRon)
                .build();

        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }


}
