package org.poo.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.poo.accounts.Account;
import org.poo.accounts.SavingsAccount;
import org.poo.cards.Card;
import org.poo.fileio.CommandInput;
import org.poo.fileio.CommerciantInput;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.UserInput;
import org.poo.reports.ReportGenerator;
import org.poo.commerciants.Commerciant;
import org.poo.transactions.Transaction;
import org.poo.users.User;

import java.util.ArrayList;

import static org.poo.utils.Utils.MIN_BALANCE_DIFFERENCE;

@Getter
public final class Bank extends ExchangeRate {
    private final ArrayList<User> users;
    private final ArrayList<Commerciant> commerciants;
    private static Bank bank;
    private static final double MIN_AGE_REQUIRED = 21;

    private Bank(final UserInput[] users, final ExchangeInput[] exchangeRates,
                 final CommerciantInput[] commerciants) {
        super(exchangeRates);
        this.users = new ArrayList<>();
        for (UserInput user : users) {
            this.users.add(new User(user, this));
        }

        this.commerciants = new ArrayList<>();
        for (CommerciantInput commerciant : commerciants) {
            this.commerciants.add(new Commerciant(commerciant));
        }
    }

    /**
     * Static method to create a unique bank instance (singleton pattern)
     * @param users the users of the bank
     * @param exchangeRates the exchange rates of the bank
     * @return the bank instance
     */
    public static Bank getInstance(final UserInput[] users, final ExchangeInput[] exchangeRates,
                                   final CommerciantInput[] commerciants) {
        if (bank == null) {
            bank = new Bank(users, exchangeRates, commerciants);
        }
        return bank;
    }

    /**
     * Resets the bank instance as every time the program is run, a new bank instance is created
     */
    public static void resetBank() {
        bank = null;
    }

    /**
     * Deposit money in the first classic account of the user with the given currency
     * @param commandInput the command input containing the deposit details
     * @param user the user that deposit the money and has the classic account
     * @param savingsAccount the savings account from which the money is withdrawn
     */
    public void depositAmountInClassicAccount(final CommandInput commandInput, final User user,
                                              final Account savingsAccount) {
        String currency = commandInput.getCurrency();
        for (Account classicAccount : user.getAccounts()) {
            if (classicAccount.isClassicAccount()
                    && classicAccount.getCurrency().equals(currency)) {
                classicAccount.deposit(commandInput.getAmount());

                // create transaction
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setTimestamp(commandInput.getTimestamp())
                        .setDescription("Savings withdrawal")
                        .setAmountWithdrawn(commandInput.getAmount())
                        .setClassicAccountIban(classicAccount.getIban())
                        .setSavingsAccountIban(savingsAccount.getIban())
                        .build();

                // add to the classic account
                user.addTransaction(transaction);
                classicAccount.addTransaction(transaction);

                // add to the savings account
                user.addTransaction(transaction);
                savingsAccount.addTransaction(transaction);
                return;
            }
        }
    }

    /**
     * Withdraw money from a savings account and deposit it in the classic account of the user
     * @param commandInput the command input containing the withdrawal details
     */
    public void withdrawSavings(final CommandInput commandInput) {
        User user = this.getUserWithAccount(commandInput.getAccount());
        Account savingsAccount = this.getAccountWithIBAN(commandInput.getAccount());

        if (user == null || savingsAccount == null) {
            return;
        }

        if (user.getAge() < MIN_AGE_REQUIRED) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setTimestamp(commandInput.getTimestamp())
                    .setDescription("You don't have the minimum age required.")
                    .build();

            user.addTransaction(transaction);
            return;
        }

        if (!user.hasClassicAccount()) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setTimestamp(commandInput.getTimestamp())
                    .setDescription("You do not have a classic account.")
                    .build();

            user.addTransaction(transaction);
            savingsAccount.addTransaction(transaction);
            return;
        }

        if (!savingsAccount.isSavingAccount()) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setTimestamp(commandInput.getTimestamp())
                    .setDescription("Account is not of type savings.")
                    .build();
            savingsAccount.addTransaction(transaction);
            return;
        }

        // extract amount from savings account
        double exchangeRate;
        try {
            exchangeRate = this.getExchangeRate(commandInput.getCurrency(),
                                                savingsAccount.getCurrency());
        } catch (Exception e) {
            return;
        }
        double convertedAmount = commandInput.getAmount() * exchangeRate;

        if (!savingsAccount.hasEnoughBalance(convertedAmount)) {
            return;
        }

        savingsAccount.withdraw(convertedAmount);
        depositAmountInClassicAccount(commandInput, user, savingsAccount);
    }


    /**
     * Generates a report using the given report generator (Strategy pattern)
     * @param commandInput the command input containing the report details
     * @param mapper the object mapper
     * @param reportGenerator the report generator used to generate the report type wanted
     * @return the report as an ObjectNode
     * @throws Exception if an error occurs while generating the report
     */
    public ObjectNode generateReport(final CommandInput commandInput,
                                     final ObjectMapper mapper,
                                     final ReportGenerator reportGenerator) throws Exception {
        if (reportGenerator == null) {
            return null;
        }

        try {
            return reportGenerator.generateReport(commandInput, mapper);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Checks the status of a card and freezes it if the balance is below the minimum
     * @param cardNumber the card number whose status is checked
     * @param timestamp the timestamp of the command
     * @throws Exception if the card is not found
     */
    public void checkCardStatus(final String cardNumber, final int timestamp) throws Exception {
        Account account = getAccountWithCard(cardNumber);
        if (account == null) {
            throw new Exception("Card not found");
        }

        // Freeze
        if (account.getBalance() <= account.getMinBalance()) {
            Card card = account.getCard(cardNumber);
            if (card == null) {
                throw new Exception("Card not found");
            }

            card.freezeCard();

            User user = getUserWithAccount(account.getIban());
            if (user == null) {
                return;
            }
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("You have reached the minimum amount of funds, "
                            + "the card will be frozen")
                    .setTimestamp(timestamp)
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
            return;
        }

        // Warning
        if (account.getBalance() <= account.getMinBalance() + MIN_BALANCE_DIFFERENCE) {
            Card card = account.getCard(cardNumber);
            if (card == null) {
                throw new Exception("Card not found");
            }

            card.warnCard();

            User user = getUserWithAccount(account.getIban());
            if (user == null) {
                return;
            }
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("You are close to reaching the minimum amount of funds")
                    .setTimestamp(timestamp)
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
        }
    }

    /**
     * Changes the interest rate of a savings account
     * @param accountIBAN the IBAN of the account whose interest rate is changed
     * @param newInterestRate the new interest rate
     * @param timestamp the timestamp of the command
     * @throws Exception if the account is not found or if it is not a savings account
     */
    public void changeInterestRate(final String accountIBAN, final double newInterestRate,
                                   final int timestamp) throws Exception {
        Account account = getAccountWithIBAN(accountIBAN);
        if (account == null) {
            throw new Exception("Account not found");
        }
        User user = getUserWithAccount(accountIBAN);
        if (user == null) {
            return;
        }

        if (account.isSavingAccount()) {
            ((SavingsAccount) account).setInterestRate(newInterestRate, user, timestamp);
        } else {
            throw new Exception("This is not a savings account");
        }
    }

    /**
     * Adds interest to a savings account
     * @param accountIBAN the IBAN of the account whose interest is added
     * @throws Exception if the account is not found or if it is not a savings account
     */
    public void addInterest(final String accountIBAN, final int timestamp) throws Exception {
        Account account = getAccountWithIBAN(accountIBAN);
        if (account == null) {
            throw new Exception("Account not found");
        }

        if (account.isSavingAccount()) {
            double interest = ((SavingsAccount) account).addInterest();

            // add transaction
            User user = getUserWithAccount(accountIBAN);
            if (user == null) {
                return;
            }
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setAmountInterest(interest)
                    .setCurrencyAddInterest(account.getCurrency())
                    .setDescription("Interest rate income")
                    .setTimestamp(timestamp)
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
        } else {
            throw new Exception("This is not a savings account");
        }
    }

    /**
     * Returns the commerciant with the given name from the bank
     * @param name the name of the commerciant
     */
    public Commerciant getCommerciantWithName(final String name) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getName().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    /**
     * @param iban the IBAN of the commerciant
     * @return the commerciant with the given IBAN or null if it does not exist
     */
    public Commerciant getCommerciantWithIban(final String iban) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getAccountIban().equals(iban)) {
                return commerciant;
            }
        }
        return null;
    }

    /**
     * @param alias the alias of the account
     * @return the account with the given alias
     */
    public Account getAccountWithAlias(final String alias) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAlias() != null && account.getAlias().equals(alias)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * @param iban the IBAN of the account
     * @return the user that has the account with the given IBAN
     */
    public User getUserWithAccount(final String iban) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * @param iban the IBAN of the account
     * @return the account with the given IBAN
     */
    public Account getAccountWithIBAN(final String iban) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * @param cardNumber the card number
     * @return the account that has the card with the given card number
     */
    public Account getAccountWithCard(final String cardNumber) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        return account;
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param email the email of the user
     * @return the user with the given email
     */
    public User getUserWithEmail(final String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * @param email the email of the user to whom the account is added
     * @param account the account to be added
     * @param accountType the type of the account
     */
    public void addAccountToUser(final String email, final Account account,
                                 final String accountType) {
        User user = getUserWithEmail(email);
        if (user != null) {
            user.addAccount(account);
            if (accountType.equals("classic")) {
                user.incrementNrClassicAccounts();
            }

            // add to the transaction list
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setDescription("New account created")
                    .setTimestamp(account.getTimestamp())
                    .build();

            user.addTransaction(transaction);
            account.addTransaction(transaction);
        }
    }

    /**
     * @param objectMapper the object mapper
     * @return the array node with the users
     */
    public ArrayNode usersTransformToArrayNode(final ObjectMapper objectMapper) {
        ArrayNode usersArray = objectMapper.createArrayNode();
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                usersArray.add(user.transformToAnObjectNode(objectMapper));
            }
        }
        return usersArray;
    }
}
