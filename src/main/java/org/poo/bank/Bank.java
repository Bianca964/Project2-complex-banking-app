package org.poo.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.poo.accounts.Account;
import org.poo.accounts.SavingsAccount;
import org.poo.cards.Card;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ExchangeInput;
import org.poo.fileio.UserInput;
import org.poo.reports.ClassicReport;
import org.poo.reports.ReportGenerator;
import org.poo.reports.SpendingsReport;
import org.poo.transactions.Transaction;

import java.util.ArrayList;

import static org.poo.utils.Utils.MIN_BALANCE_DIFFERENCE;

@Getter
public final class Bank extends ExchangeRate {
    private final ArrayList<User> users;
    private static Bank bank;

    private Bank(final UserInput[] users, final ExchangeInput[] exchangeRates) {
        super(exchangeRates);
        this.users = new ArrayList<>();
        for (UserInput user : users) {
            this.users.add(new User(user));
        }
    }

    /**
     * Static method to create a unique bank instance (singleton pattern)
     * @param users the users of the bank
     * @param exchangeRates the exchange rates of the bank
     * @return the bank instance
     */
    public static Bank getInstance(final UserInput[] users, final ExchangeInput[] exchangeRates) {
        if (bank == null) {
            bank = new Bank(users, exchangeRates);
        }
        return bank;
    }

    /**
     * Resets the bank instance as every time the program is run, a new bank instance is created
     */
    public static void resetBank() {
        bank = null;
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

        if (!user.hasClassicAccount()) {
            Transaction transaction = new Transaction.TransactionBuilder()
                    .setTimestamp(commandInput.getTimestamp())
                    .setDescription("You do not have a classic account.")
                    .build();

            user.addTransaction(transaction);
            return;
        }

        if (savingsAccount == null) {
            throw new Exception("Account not found");
        }

        if (!savingsAccount.hasInterest()) {
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

        // tb convertita suma
        if (!savingsAccount.hasEnoughBalance(convertedAmount)) {
            throw new Exception("Insufficient funds");
        }

        System.out.println("Savings account balance before: " + savingsAccount.getBalance());
        savingsAccount.withdraw(convertedAmount);
        System.out.println("Savings account balance after: " + savingsAccount.getBalance());

        // deposit amount in classic account
        String currency = commandInput.getCurrency();

        for (Account account : user.getAccounts()) {
            // if it s a classic account
            if (!account.hasInterest() && account.getCurrency().equals(currency)) {
                System.out.println("Classic account balance before: " + account.getBalance());
                account.deposit(commandInput.getAmount());
                System.out.println("Classic account balance after: " + account.getBalance());

                // create transaction
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setTimestamp(commandInput.getTimestamp())
                        .setDescription("Savings withdrawal")
                        .build();
                user.addTransaction(transaction);
                account.addTransaction(transaction);
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

        if (account.hasInterest()) {
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
    public void addInterest(final String accountIBAN) throws Exception {
        Account account = getAccountWithIBAN(accountIBAN);
        if (account == null) {
            throw new Exception("Account not found");
        }

        if (account.hasInterest()) {
            ((SavingsAccount) account).addInterest();
        } else {
            throw new Exception("This is not a savings account");
        }
    }

    /**
     * @param iban the IBAN of the account whose alias is set
     * @param alias the new alias
     */
    public void setAlias(final String iban, final String alias) {
        Account account = getAccountWithIBAN(iban);
        if (account != null) {
            account.setAlias(alias);
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
     */
    public void addAccountToUser(final String email, final Account account, final String accountType) {
        User user = getUserWithEmail(email);
        if (user != null) {
            user.addAccount(account);
            if (accountType.equals("classic")) {
                user.setHasClassicAccount();
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
