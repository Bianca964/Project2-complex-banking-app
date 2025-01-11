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
import org.poo.reports.BusinessReport;
import org.poo.reports.ClassicReport;
import org.poo.reports.ReportGenerator;
import org.poo.reports.SpendingsReport;
import org.poo.transactions.Commerciant;
import org.poo.transactions.Transaction;
import org.poo.users.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static org.poo.utils.Utils.MIN_BALANCE_DIFFERENCE;

@Getter
public final class Bank extends ExchangeRate {
    private final ArrayList<User> users;
    private ArrayList<Commerciant> commerciants;
    private static Bank bank;

    private Bank(final UserInput[] users, final ExchangeInput[] exchangeRates, final CommerciantInput[] commerciants) {
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
    public static Bank getInstance(final UserInput[] users, final ExchangeInput[] exchangeRates, final CommerciantInput[] commerciants) {
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



    public Commerciant getCommerciantWithName(final String name) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getName().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    public void withdrawSavings(CommandInput commandInput) throws Exception {

        User user = this.getUserWithAccount(commandInput.getAccount());
        Account savingsAccount = this.getAccountWithIBAN(commandInput.getAccount());

        if (user == null) {
            return;
        }

        if (user.getAge() < 21) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setTimestamp(commandInput.getTimestamp())
                    .setDescription("You don't have the minimum age required.")
                    .build();

            user.addTransaction(transaction);
            return;
        }



        if (savingsAccount == null) {
            throw new Exception("Account not found");
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
        double exchangeRate = this.getExchangeRate(commandInput.getCurrency(), savingsAccount.getCurrency());
        double convertedAmount = commandInput.getAmount() * exchangeRate;

        if (!savingsAccount.hasEnoughBalance(convertedAmount)) {
            throw new Exception("Insufficient funds");
        }

        savingsAccount.withdraw(convertedAmount);

        // deposit amount in classic account
        String currency = commandInput.getCurrency();
        for (Account classicAccount : user.getAccounts()) {
            // if it s a classic account
            if (classicAccount.isClassicAccount() && classicAccount.getCurrency().equals(currency)) {
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
     * @param commandInput the object with the whole input
     * @param mapper the object mapper
     * @return the object node with the classic report
     * @throws Exception if the account is not found
     */
    public ObjectNode report(final CommandInput commandInput,
                             final ObjectMapper mapper) throws Exception {
        ReportGenerator reportGenerator = new ClassicReport(this);
        return reportGenerator.generateReport(commandInput, mapper);
    }

    /**
     * @param commandInput the object with the whole input
     * @param mapper the object mapper
     * @return the object node with the spendings report
     * @throws Exception if the account is not found
     */
    public ObjectNode spendingsReport(final CommandInput commandInput,
                                      final ObjectMapper mapper) throws Exception {
        ReportGenerator reportGenerator = new SpendingsReport(this);
        return reportGenerator.generateReport(commandInput, mapper);
    }

    public ObjectNode businessReport(final CommandInput commandInput,
                                      final ObjectMapper mapper) throws Exception {
        ReportGenerator reportGenerator = new BusinessReport(this);
        return reportGenerator.generateReport(commandInput, mapper);
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

        System.out.println("tp " + timestamp + " balance of the account: " + account.getBalance() + " vs min balance: " + account.getMinBalance());

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

            //BigDecimal roundedInterest = new BigDecimal(interest).setScale(2, RoundingMode.HALF_UP);

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



    public Commerciant getCommerciantWithIban(final String iban) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getAccountIban().equals(iban)) {
                return commerciant;
            }
        }
        return null;
    }



    /**
     * @param iban the IBAN of the account whose alias is set
     * @param alias the new alias
     */
    public void setAlias(final String iban, final String alias, final User user) {
        Account account = getAccountWithIBAN(iban);
        if (account != null) {
            account.setAlias(alias, user);
        }
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
    public void addAccountToUser(final String email, final Account account, final String accountType) {
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
