package org.poo.transactions;

import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.cards.OneTimeCard;
import org.poo.serviceplans.ServicePlan;
import org.poo.users.User;
import org.poo.cards.Card;
import org.poo.fileio.CommandInput;


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

        Account account = bank.getAccountWithCard(command.getCardNumber());
        if (account == null) {
            throw new Exception("Card not found");
        }

        Card card = account.getCard(command.getCardNumber());
        if (card == null) {
            throw new Exception("Card not found");
        }

        // if user is not associate or owner of the business account
        if (account.isBusinessAccount()) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.isOwner(user) && !businessAccount.isAssociate(user)) {
                throw new Exception("Card not found");
            }
        } else {
            // daca contul e unul normal si nu apartine celui care face plata
            user = bank.getUserWithAccount(account.getIban());
            if (user == null || !user.getEmail().equals(command.getEmail())) {
                throw new Exception("Card not found");
            }
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

        // convert amount to account currency and add comission
        double exchangeRate = bank.getExchangeRate(command.getCurrency(), account.getCurrency());
        double amountInAccountCurrency = command.getAmount() * exchangeRate;
        double amountWithComission = user.getServicePlan().applyComission(amountInAccountCurrency, account.getCurrency());


        if (account.isBusinessAccount()) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            User owner = businessAccount.getOwner();
            amountWithComission = owner.getServicePlan().applyComission(amountInAccountCurrency, businessAccount.getCurrency());

            if (businessAccount.hasEnoughBalance(amountWithComission)) {
                try {
                    businessAccount.increaseAmountSpentOnBusinessAccountByUser(amountInAccountCurrency, user);
                } catch (Exception e) {
                    Transaction transaction = new Transaction.TransactionBuilder()
                            .setDescription("Spending limit exceeded")
                            .setTimestamp(command.getTimestamp())
                            .build();

                    // add transaction to the account
                    businessAccount.addTransaction(transaction);

                    // add transaction to the user in bank
                    user.addTransaction(transaction);
                    return;
                }

                // withdraw the amount from the account
                businessAccount.withdraw(amountWithComission);

                // the money sent by the owner of the account doesn't count
                if (!businessAccount.isOwner(user)) {
                    // if the commerciant is not in the list of commerciants of the account, add it
                    if (!businessAccount.hasCommerciantAddedByAssociate(commerciant)) {
                        businessAccount.addCommerciantAddedByAssociate(commerciant);
                    }
                    // add the received amount to the commerciant
                    businessAccount.addAmountReceivedByCommerciant(amountInAccountCurrency, commerciant);
                    businessAccount.addAssociateToCommerciant(user, commerciant);
                }

            } else {
                // CONTUL NU ARE SUFIECIENTE FONDURI
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setDescription("Insufficient funds")
                        .setTimestamp(command.getTimestamp())
                        .build();

                user.addTransaction(transaction);
                account.addTransaction(transaction);
                return;
            }

        } else {
            // NU E BUSINESS ACCOUNT
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
        }


        // increase the number of payments of at least 300 RON (useful for the upgradePlan case)
        double amountInRon = command.getAmount() * bank.getExchangeRate(command.getCurrency(), "RON");
        if (account.isBusinessAccount()) {
            user = ((BusinessAccount) account).getOwner();
        }
        if (user.getServicePlan().getName().equals("silver") && amountInRon >= 300) {
            user.increaseMin300payments();
        }



        card.handlePostPayment(account, user, command, amountInAccountCurrency);



        // CASHBACK

        System.out.println("--------------------");
        System.out.println("payOnline timestamp " + command.getTimestamp() + " paid converted amount " + amountInAccountCurrency + account.getCurrency() + " amount in RON" + amountInRon + " to " + commerciant.getName() + " card " +
                card.getCardNumber() + " from account " + account.getIban() + " balance before cashback " + account.getBalance() + account.getCurrency() + " plan " + user.getServicePlan().getName() + " the comission is " + (amountWithComission - amountInAccountCurrency));

        applyCashBack(commerciant, user, account, amountInAccountCurrency, amountInRon);
        user.checkForUpgradeToGoldPlan(account, bank, command.getTimestamp());





        System.out.println("payOnline timestamp " + command.getTimestamp() + " paid converted amount " + amountInAccountCurrency + account.getCurrency() + " amount in RON" + amountInRon + " to " + commerciant.getName() + " card " +
                card.getCardNumber() + " from account " + account.getIban() + " balance after cashback " + account.getBalance() + account.getCurrency() + " plan " + user.getServicePlan().getName() + " the comission is " + (amountWithComission - amountInAccountCurrency));
        System.out.println("--------------------");




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

        // if account already has the commerciant, increment the number of transactions
        if (senderAccount.getCommerciant(commerciant) != null) {
            senderAccount.incrementNrOfTrnscForCommerciant(commerciant);
        } else {
            // if account doesn't have the commerciant, add it to account
            senderAccount.addCommerciant(commerciant);
            senderAccount.getCommerciant(commerciant).setNrTransactions(1);
        }

        // if there is a discount available, apply it and then add another if possible
        if (sender.hasDiscountAvailable()) {
            sender.applyDiscount(senderAccount, commerciant, amountInAccountCurrency);
        }

        if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
//            // if there is a discount available, apply it and then add another if possible
//            if (sender.hasDiscountAvailable()) {
//                sender.applyDiscount(senderAccount, commerciant, amountInAccountCurrency);
//            }

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
//            // if there is a discount available, apply it and then add another if possible
//            if (sender.hasDiscountAvailable()) {
//                sender.applyDiscount(senderAccount, commerciant, amountInAccountCurrency);
//            }

            senderAccount.addAmountForSpendingThreshold(amountInRon);
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
        User senderUser = bank.getUserWithEmail(command.getEmail());
        User receiverUser = bank.getUserWithAccount(command.getReceiver());

        if (senderUser == null || command.getReceiver().isEmpty()) {
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

        // if the receiver is not a commerciant (receiverUser != null), find the account by alias
        if (receiverAccount == null) {
            // find the account by alias
            receiverAccount = bank.getAccountWithAlias(command.getReceiver());
            if (receiverAccount == null) {
                return;
            }
        }

        // start the transaction to another user
        // convert amount to receiver currency
        double amountSender = command.getAmount();
        double exchangeRate = bank.getExchangeRate(senderAccount.getCurrency(),
                receiverAccount.getCurrency());
        double amountInReceiverCurrency = command.getAmount() * exchangeRate;

        // if the senderAccount is business and the sender si employee, need to check for the spending limit
        if (senderAccount.isBusinessAccount() && ((BusinessAccount)senderAccount).isEmployee(senderUser)) {
            // if the spending limit is exceeded, add a transaction to the account and the user
            if (amountSender > ((BusinessAccount)senderAccount).getSpendingLimitForEmployees()) {
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setDescription("Spending limit exceeded")
                        .setTimestamp(command.getTimestamp())
                        .build();

                senderAccount.addTransaction(transaction);
                senderUser.addTransaction(transaction);
                return;
            }
        }

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

        // if the money is sent from a business account, the comission is from the owner
        ServicePlan servicePlanForComission = senderUser.getServicePlan();
        if (senderAccount.isBusinessAccount()) {
            servicePlanForComission = ((BusinessAccount)senderAccount).getOwner().getServicePlan();
        }

        try {
            transactionSender.doTransactionSendMoney(servicePlanForComission);
        } catch (Exception e) {
            Transaction transactionError = new Transaction.TransactionBuilder()
                    .setDescription(e.getMessage())
                    .setTimestamp(command.getTimestamp())
                    .build();

            // if the transaction failed, add it only to the sender
            senderUser.addTransaction(transactionError);
            senderAccount.addTransaction(transactionError);
            receiverAccount.addTransaction(transactionError);
            return;
        }

        // add the transactions to the sender and receiver
        senderUser.addTransaction(transactionSender);
        receiverUser.addTransaction(transactionReceiver);
        senderAccount.addTransaction(transactionSender);
        receiverAccount.addTransaction(transactionReceiver);

        // increase the number of min 300 RON payments if the sender has silver plan
        double exchangeRateRon = bank.getExchangeRate(senderAccount.getCurrency(), "RON");
        double amountInRon = command.getAmount() * exchangeRateRon;

        if (senderAccount.isBusinessAccount()) {
            senderUser = ((BusinessAccount)senderAccount).getOwner();
        }
        if (senderUser.getServicePlan().getName().equals("silver") && amountInRon >= 300) {
            senderUser.increaseMin300payments();
        }
        senderUser.checkForUpgradeToGoldPlan(senderAccount, bank, command.getTimestamp());
    }






    public void sendMoneyToCommerciant(User senderUser, Account senderAccount, Commerciant receiverCommerciant, CommandInput command) {
        double amountSender = command.getAmount();
        double amountSenderWithComission = senderUser.getServicePlan().applyComission(amountSender, senderAccount.getCurrency());
        // if the money are sent from a business account, the comission is from the owner
        if (senderAccount.isBusinessAccount()) {
            amountSenderWithComission = ((BusinessAccount)senderAccount).getOwner().getServicePlan().applyComission(amountSender, senderAccount.getCurrency());
        }

        if (senderAccount.hasEnoughBalance(amountSenderWithComission)) {
            senderAccount.withdraw(amountSenderWithComission);

            // add the received amount to the commerciant
            if (senderAccount.isBusinessAccount()) {
                BusinessAccount businessAccount = (BusinessAccount) senderAccount;

                // the money sent by the owner of the account doesn't count
                if (!businessAccount.isOwner(senderUser)) {
                    // if the commerciant is not in the list of commerciants of the account, add it
                    if (!businessAccount.hasCommerciantAddedByAssociate(receiverCommerciant)) {
                        businessAccount.addCommerciantAddedByAssociate(receiverCommerciant);
                    }
                    // add the received amount to the commerciant
                    businessAccount.addAmountReceivedByCommerciant(amountSender, receiverCommerciant);
                    businessAccount.addAssociateToCommerciant(senderUser, receiverCommerciant);
                }
            }
        } else {
            // not everyone has enough funds
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("Insufficient funds")
                    .setTimestamp(command.getTimestamp())
                    .build();

            senderUser.addTransaction(transaction);
            senderAccount.addTransaction(transaction);
            return;
        }

        // add transaction to sender user
        Transaction transactionSender = new Transaction.TransactionBuilder()
                .setDescription(command.getDescription())
                .setFromAccount(senderAccount)
                .setToAccountCommerciant(receiverCommerciant.getAccountIban())
                .setAmountSender(command.getAmount())
                .setTimestamp(command.getTimestamp())
                .setTransferType("sent")
                .build();

        senderUser.addTransaction(transactionSender);

        // cashback
        double exchangeRate;
        try {
            exchangeRate = bank.getExchangeRate(senderAccount.getCurrency(), "RON");
        } catch (Exception e) {
            return;
        }
        double amountInRon = amountSender * exchangeRate;
        applyCashBack(receiverCommerciant, senderUser, senderAccount, amountSender, amountInRon);

        // increase the number of min 300 RON payments if the sender has silver plan
        if (senderAccount.isBusinessAccount()) {
            senderUser = ((BusinessAccount)senderAccount).getOwner();
        }
        if (senderUser.getServicePlan().getName().equals("silver") && amountInRon >= 300) {
            senderUser.increaseMin300payments();
        }
        senderUser.checkForUpgradeToGoldPlan(senderAccount, bank, command.getTimestamp());

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

        // if the account is business and the user is not associated with the account
        if (account.isBusinessAccount()) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.isOwner(user) && !businessAccount.isAssociate(user)) {
                throw new Exception("Card not found");
            }
        } else {
            // if the one trying to withdraw money is not the owner of the account
            user = bank.getUserWithAccount(account.getIban());
            if (user == null || !user.getEmail().equals(commandInput.getEmail())) {
                throw new Exception("Card not found");
            }
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

        // if the one doing cash withdrawal is an employee of a business account
        if (account.isBusinessAccount()) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (businessAccount.isEmployee(user)) {
                if (amountInRon > businessAccount.getSpendingLimitForEmployees()) {
                    Transaction transaction = new Transaction.TransactionBuilder()
                            .setDescription("Spending limit exceeded")
                            .setTimestamp(commandInput.getTimestamp())
                            .build();

                    user.addTransaction(transaction);
                    account.addTransaction(transaction);
                    return;
                }
            }
        }

        // withdraw cash from the account
        account.withdraw(amountWithComission);
        Transaction transaction = new Transaction.TransactionBuilder()
                .setDescription("Cash withdrawal of " + commandInput.getAmount())
                .setTimestamp(commandInput.getTimestamp())
                .setAmountCashWithdrawal(amountInRon)
                .build();

        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }
}
