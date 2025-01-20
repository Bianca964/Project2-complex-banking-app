package org.poo.transactions;

import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.cashback.CashbackStrategyContext;
import org.poo.commerciants.Commerciant;
import org.poo.serviceplans.ServicePlan;
import org.poo.users.User;
import org.poo.cards.Card;
import org.poo.fileio.CommandInput;

public class TransactionService {
    private static final int MIN_AMOUNT_SILVER_PLAN = 300;

    private final Bank bank;
    private final CashbackStrategyContext cashbackStrategyContext;

    public TransactionService(final Bank bank) {
        this.bank = bank;
        this.cashbackStrategyContext = new CashbackStrategyContext();
    }

    /**
     * Executes the pay online command to a commerciant
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

        try {
            checkCardNotFoundCases(command, user);
        } catch (Exception e) {
            throw new Exception("Card not found");
        }

        Account account = bank.getAccountWithCard(command.getCardNumber());
        // if the account would be null, it would have thrown the exception "Card not found"
        assert account != null;
        Card card = account.getCard(command.getCardNumber());

        // if the card is frozen, don't do the transaction
        if (card.isFrozen()) {
            addCardIsFrozenTransaction(user, account, command.getTimestamp());
            return;
        }

        Commerciant commerciant = bank.getCommerciantWithName(command.getCommerciant());
        if (commerciant == null) {
            throw new Exception("Commerciant not found");
        }

        // convert amount to account currency and add commission
        double exchangeRate = bank.getExchangeRate(command.getCurrency(), account.getCurrency());
        double amountInAccountCurrency = command.getAmount() * exchangeRate;
        double amountWithCommission = user.getServicePlan().applyCommission(amountInAccountCurrency,
                                                                            account.getCurrency());

        if (account.isBusinessAccount()) {
            try {
                handlePayOnlineForBusinessAccount(account, user, commerciant,
                                                  amountInAccountCurrency, command.getTimestamp());
            } catch (Exception e) {
                return;
            }
        } else {
            // not a business account
            if (account.hasEnoughBalance(amountWithCommission)) {
                account.withdraw(amountWithCommission);
            } else {
                addInsufficientFundsTransaction(user, account, command.getTimestamp());
                return;
            }
        }

        card.handlePostPayment(account, user, command, amountInAccountCurrency);

        // CASHBACK
        exchangeRate = bank.getExchangeRate(command.getCurrency(), "RON");
        double amountInRon = command.getAmount() * exchangeRate;
        applyCashback(commerciant, user, account, amountInAccountCurrency, amountInRon);

        handlePotentialUpgradeToGoldPlan(account, user, command.getTimestamp(), amountInRon);
    }

    /**
     * Handles the pay online command for a business account
     * @param account the account from which the payment is made
     * @param user the user that makes the payment
     * @param commerciant the commerciant that receives the payment
     * @param timestamp the timestamp of the payment
     * @param amountInAccountCurrency the amount of the payment in the account currency
     * @throws Exception if the account doesn't have enough funds or the spending limit is exceeded
     */
    public void handlePayOnlineForBusinessAccount(final Account account, final User user,
                                                  final Commerciant commerciant,
                                                  final double amountInAccountCurrency,
                                                  final int timestamp) throws Exception {
        BusinessAccount businessAcc = (BusinessAccount) account;
        ServicePlan servicePlan = businessAcc.getOwner().getServicePlan();
        double amountWithCommission = servicePlan.applyCommission(amountInAccountCurrency,
                                                                  businessAcc.getCurrency());

        if (businessAcc.hasEnoughBalance(amountWithCommission)) {
            try {
                businessAcc.increaseAmountSpentByUser(amountInAccountCurrency, user);
            } catch (Exception e) {
                addSpendingLimitExceededTransaction(user, businessAcc, timestamp);
                throw new Exception("Spending limit exceeded");
            }

            // withdraw the amount from the account
            businessAcc.withdraw(amountWithCommission);

            // the money sent by the owner of the account doesn't count
            if (!businessAcc.isOwner(user)) {
                // if the commerciant is not in the list of commerciants of the account, add it
                if (!businessAcc.hasCommerciantAddedByAssociate(commerciant)) {
                    businessAcc.addCommerciantAddedByAssociate(commerciant);
                }
                // add the received amount to the commerciant
                businessAcc.addAmountReceivedByCommerciant(amountInAccountCurrency, commerciant);
                businessAcc.addAssociateToCommerciant(user, commerciant);
            }
        } else {
            // the account doesn't have enough funds
            addInsufficientFundsTransaction(user, account, timestamp);
            throw new Exception("Insufficient funds");
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
        double amountSender = command.getAmount();
        double exchangeRate = bank.getExchangeRate(senderAccount.getCurrency(),
                receiverAccount.getCurrency());
        double amountInReceiverCurrency = command.getAmount() * exchangeRate;

        // if the senderAccount is business, need to check for the spending limit
        if (senderAccount.isBusinessAccount()
                && ((BusinessAccount) senderAccount).isEmployee(senderUser)) {
            // if the spending limit is exceeded, add a transaction to the account and the user
            if (amountSender > ((BusinessAccount) senderAccount).getSpendingLimit()) {
                int timestamp = command.getTimestamp();
                addSpendingLimitExceededTransaction(senderUser, senderAccount, timestamp);
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

        // if the money is sent from a business account, the commission is from the owner
        ServicePlan servicePlanForComission = senderUser.getServicePlan();
        if (senderAccount.isBusinessAccount()) {
            servicePlanForComission = ((BusinessAccount) senderAccount).getOwner().getServicePlan();
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

        double exchangeRateRon = bank.getExchangeRate(senderAccount.getCurrency(), "RON");
        double amountInRon = command.getAmount() * exchangeRateRon;
        int timestamp = command.getTimestamp();
        handlePotentialUpgradeToGoldPlan(senderAccount, senderUser, timestamp, amountInRon);
    }

    /**
     * Executes the transfer command to a commerciant
     * @param senderUser the user that sends the money
     * @param senderAccount the account from which the money is sent
     * @param receiverCommerciant the commerciant that receives the money
     * @param command the object with the whole input
     */
    public void sendMoneyToCommerciant(final User senderUser, final Account senderAccount,
                                       final Commerciant receiverCommerciant,
                                       final CommandInput command) {
        double amountSender = command.getAmount();
        ServicePlan servicePlan = senderUser.getServicePlan();
        double amountSenderWithComission = servicePlan.applyCommission(amountSender,
                                                                       senderAccount.getCurrency());
        // if the money are sent from a business account, the commission is from the owner
        if (senderAccount.isBusinessAccount()) {
            User owner = ((BusinessAccount) senderAccount).getOwner();
            servicePlan = owner.getServicePlan();
            amountSenderWithComission = servicePlan.applyCommission(amountSender,
                                                                    senderAccount.getCurrency());
        }

        if (senderAccount.hasEnoughBalance(amountSenderWithComission)) {
            senderAccount.withdraw(amountSenderWithComission);

            // add the received amount to the commerciant
            if (senderAccount.isBusinessAccount()) {
                BusinessAccount businessAcc = (BusinessAccount) senderAccount;

                // the money sent by the owner of the account doesn't count
                if (!businessAcc.isOwner(senderUser)) {
                    // if the commerciant is not in the list of commerciants of the account, add it
                    if (!businessAcc.hasCommerciantAddedByAssociate(receiverCommerciant)) {
                        businessAcc.addCommerciantAddedByAssociate(receiverCommerciant);
                    }
                    // add the received amount to the commerciant
                    businessAcc.addAmountReceivedByCommerciant(amountSender, receiverCommerciant);
                    businessAcc.addAssociateToCommerciant(senderUser, receiverCommerciant);
                }
            }
        } else {
            // not everyone has enough funds
            addInsufficientFundsTransaction(senderUser, senderAccount, command.getTimestamp());
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
        applyCashback(receiverCommerciant, senderUser, senderAccount, amountSender, amountInRon);

        int timestamp = command.getTimestamp();
        handlePotentialUpgradeToGoldPlan(senderAccount, senderUser, timestamp, amountInRon);
    }

    /**
     *
     * @param commerciant to which the payment was made
     * @param sender the one who made the payment
     * @param senderAccount the account from which the payment was made (receives the cashback)
     * @param amountInAccountCurrency the amount of the payment in the sender's account currency
     * @param amountInRon the amount of the payment in RON
     */
    public void applyCashback(final Commerciant commerciant, final User sender,
                              final Account senderAccount, final double amountInAccountCurrency,
                              final double amountInRon) {
        cashbackStrategyContext.setCashbackStrategy(commerciant);
        cashbackStrategyContext.applyCashback(commerciant, sender, senderAccount,
                                              amountInAccountCurrency, amountInRon);
    }

    /**
     * Handles the potential upgrade to gold plan after a transaction
     * @param senderAccount the account from which the payment was made
     * @param senderUser the user that made the payment
     * @param timestamp the timestamp of the payment
     * @param amountInRon the amount of the payment in RON
     */
    public void handlePotentialUpgradeToGoldPlan(final Account senderAccount, final User senderUser,
                                                 final int timestamp, final double amountInRon) {
        User user;
        if (senderAccount.isBusinessAccount()) {
            user = ((BusinessAccount) senderAccount).getOwner();
        } else {
            user = senderUser;
        }

        // increase the number of min 300 RON payments if the sender has silver plan
        if (user.getServicePlan().isSilverPlan() && amountInRon >= MIN_AMOUNT_SILVER_PLAN) {
            user.increaseMin300payments();
        }
        user.checkForUpgradeToGoldPlan(senderAccount, bank, timestamp);
    }

    /**
     * Executes the cash withdrawal from an account
     * @param commandInput the object with the whole input
     * @throws Exception if the user or the account is not found
     */
    public void cashWithdrawal(final CommandInput commandInput) throws Exception {
        User user = bank.getUserWithEmail(commandInput.getEmail());
        if (user == null) {
            throw new Exception("User not found");
        }

        try {
            checkCardNotFoundCases(commandInput, user);
        } catch (Exception e) {
            throw new Exception("Card not found");
        }

        Account account = bank.getAccountWithCard(commandInput.getCardNumber());
        assert account != null;
        Card card = account.getCard(commandInput.getCardNumber());

        // if the card is frozen, don't do the transaction
        if (card.isFrozen()) {
            addCardIsFrozenTransaction(user, account, commandInput.getTimestamp());
            return;
        }

        double amountInRon = commandInput.getAmount();
        double exchangeRate = bank.getExchangeRate("RON", account.getCurrency());
        double amountInAccountCurrency = amountInRon * exchangeRate;
        double amountWithCommission = user.getServicePlan().applyCommission(amountInAccountCurrency,
                                                                            account.getCurrency());
        if (!account.hasEnoughBalance(amountWithCommission)) {
            addInsufficientFundsTransaction(user, account, commandInput.getTimestamp());
            return;
        }

        if (account.getBalance() - amountWithCommission < account.getMinBalance()) {
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
                if (amountInRon > businessAccount.getSpendingLimit()) {
                    addSpendingLimitExceededTransaction(user, account, commandInput.getTimestamp());
                    return;
                }
            }
        }

        // withdraw cash from the account
        account.withdraw(amountWithCommission);
        Transaction transaction = new Transaction.TransactionBuilder()
                .setDescription("Cash withdrawal of " + commandInput.getAmount())
                .setTimestamp(commandInput.getTimestamp())
                .setAmountCashWithdrawal(amountInRon)
                .build();

        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * Adds a transaction to the user and the account when there are insufficient funds
     * @param user the user to which the transaction is added
     * @param account the account to which the transaction is added
     * @param timestamp the timestamp of the transaction
     */
    public void addInsufficientFundsTransaction(final User user, final Account account,
                                                final int timestamp) {
        Transaction transaction = new Transaction.TransactionBuilder()
                .setDescription("Insufficient funds")
                .setTimestamp(timestamp)
                .build();

        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * Adds a transaction to the user and the account when the spending limit is exceeded
     * @param user the user to which the transaction is added
     * @param account the account to which the transaction is added
     * @param timestamp the timestamp of the transaction
     */
    public void addSpendingLimitExceededTransaction(final User user, final Account account,
                                                    final int timestamp) {
        Transaction transaction = new Transaction.TransactionBuilder()
                .setDescription("Spending limit exceeded")
                .setTimestamp(timestamp)
                .build();

        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * Adds a transaction to the user and the account when the card is frozen
     * @param user the user that has the card
     * @param account the account that has the card
     * @param timestamp the timestamp of the transaction
     */
    public void addCardIsFrozenTransaction(final User user, final Account account,
                                           final int timestamp) {
        Transaction transaction = new Transaction.TransactionBuilder()
                .setDescription("The card is frozen")
                .setTimestamp(timestamp)
                .build();

        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * Checks the cases when the card is not found for the pay online and cash withdrawal commands
     * @param commandInput the object with the whole input
     * @param user the user that makes the payment
     * @throws Exception if the card is not found
     */
    public void checkCardNotFoundCases(final CommandInput commandInput,
                                       final User user) throws Exception {
        Account account = bank.getAccountWithCard(commandInput.getCardNumber());
        if (account == null) {
            throw new Exception("Card not found");
        }

        Card card = account.getCard(commandInput.getCardNumber());
        if (card == null) {
            throw new Exception("Card not found");
        }

        // if the account is business and the user is not associated with the account
        if (account.isBusinessAccount()) {
            BusinessAccount businessAccount = (BusinessAccount) account;
            if (!businessAccount.isOwner(user) && !businessAccount.isAssociate(user)) {
                throw new Exception("Card not found");
            }
        } else {
            // if the account is not a business account and the user is not the owner
            User accOwner = bank.getUserWithAccount(account.getIban());
            if (accOwner == null || !accOwner.getEmail().equals(commandInput.getEmail())) {
                throw new Exception("Card not found");
            }
        }
    }
}
