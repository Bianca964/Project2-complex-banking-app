package org.poo.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.cards.Card;
import org.poo.fileio.UserInput;
import org.poo.transactions.Transaction;

import java.util.ArrayList;

import static org.poo.utils.Utils.generateCardNumber;

public class User {
    private final UserInput userInfo;
    @Getter
    @Setter
    private ArrayList<Account> accounts;
    private final ArrayList<Transaction> transactions;
    private boolean hasClassicAccount;

    public User(final UserInput userInfo) {
        this.userInfo = userInfo;
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.hasClassicAccount = false;
    }

    public void setHasClassicAccount() {
        this.hasClassicAccount = true;
    }

    public boolean hasClassicAccount() {
        return hasClassicAccount;
    }

    /**
     * @param cardNumber the card number which will be deleted
     * @param timestamp the timestamp of the command
     */
    public void deleteCard(final String cardNumber, final int timestamp) {
        for (Account account : accounts) {
            for (Card card : account.getCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    account.getCards().remove(card);

                    Transaction transaction = new Transaction.TransactionBuilder()
                            .setTimestamp(timestamp)
                            .setDescription("The card has been destroyed")
                            .setCardNumber(cardNumber)
                            .setCardHolderEmail(userInfo.getEmail())
                            .setAccountIBAN(account.getIban())
                            .build();

                    this.addTransaction(transaction);
                    account.addTransaction(transaction);
                    return;
                }
            }
        }
    }

    /**
     * @param iban the IBAN of the account where the card will be created
     * @param timestamp the timestamp of the command
     */
    public void createCard(final String iban, final int timestamp) {
        Account account = getAccount(iban);
        String cardNumber = generateCardNumber();

        if (account == null) {
            return;
        }
        account.createCard(cardNumber);

        Transaction transaction = new Transaction.TransactionBuilder()
                .setTimestamp(timestamp)
                .setDescription("New card created")
                .setCardNumber(cardNumber)
                .setCardHolderEmail(userInfo.getEmail())
                .setAccountIBAN(iban)
                .build();

        this.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * @param iban the IBAN of the account where the one-time card will be created
     * @param timestamp the timestamp of the command
     */
    public void createOneTimeCard(final String iban, final int timestamp) {
        Account account = getAccount(iban);
        String cardNumber = generateCardNumber();

        if (account == null) {
            return;
        }
        account.createOneTimeCard(cardNumber);

        Transaction transaction = new Transaction.TransactionBuilder()
                .setTimestamp(timestamp)
                .setDescription("New card created")
                .setCardNumber(cardNumber)
                .setCardHolderEmail(userInfo.getEmail())
                .setAccountIBAN(iban)
                .build();

        this.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * @param transaction the transaction to be added to the user's list of transactions
     */
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * @return the user's first name
     */
    public String getFirstName() {
        return userInfo.getFirstName();
    }

    /**
     * @return the user's last name
     */
    public String getLastName() {
        return userInfo.getLastName();
    }

    /**
     * @return the user's email
     */
    public String getEmail() {
        return userInfo.getEmail();
    }

    public String getBirthDate() {
        return userInfo.getBirthDate();
    }

    public String getOccupation() {
        return userInfo.getOccupation();
    }

    public int getAge() {
        return 2024 - Integer.parseInt(userInfo.getBirthDate().substring(0,4));
    }



    /**
     * @param account the account to be added to the user's list of accounts
     */
    public void addAccount(final Account account) {
        accounts.add(account);
    }

    /**
     * @param iban the IBAN of the account to be removed
     * @param timestamp the timestamp of the command
     * @throws Exception if the account couldn't be deleted
     */
    public void deleteAccount(final String iban, final int timestamp) throws Exception {
        Account account = getAccount(iban);

        if (account != null) {
            if (account.hasMoneyInAccount()) {
                // add transaction to user
                Transaction transaction = new Transaction.TransactionBuilder()
                        .setTimestamp(timestamp)
                        .setDescription("Account couldn't be deleted - there are funds remaining")
                        .build();
                this.addTransaction(transaction);
                throw new Exception("Account couldn't be deleted - see org.poo.transactions "
                        + "for details");
            }
            accounts.remove(account);
        }
    }

    /**
     * @param iban the IBAN of the account to be returned
     * @return the account with the given IBAN
     */
    public Account getAccount(final String iban) {
        for (Account account : accounts) {
            if (account.getIban().equals(iban)) {
                return account;
            }
        }
        return null;
    }

    /**
     * @param cardNumber the card number to be searched for
     * @return the account that has the card with the given card number
     */
    public Account getAccountWithCard(final String cardNumber) {
        for (Account account : accounts) {
            for (Card card : account.getCards()) {
                if (card.getCardNumber().equals(cardNumber)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Transforms the user's attributes into a JSON representation
     * @param objectMapper an instance of ObjectMapper used to create and manipulate JSON nodes
     * @return an ObjectNode representing the user's attributes
     */
    public ObjectNode transformToAnObjectNode(final ObjectMapper objectMapper) {
        ObjectNode userNode = objectMapper.createObjectNode();

        if (userInfo != null) {
            userNode.put("firstName", userInfo.getFirstName());
            userNode.put("lastName", userInfo.getLastName());
            userNode.put("email", userInfo.getEmail());
        }

        ArrayNode accountsArray = objectMapper.createArrayNode();
        if (accounts != null && !accounts.isEmpty()) {
            for (Account account : accounts) {
                accountsArray.add(account.transformToObjectNode(objectMapper));
            }
        }
        userNode.set("accounts", accountsArray);
        return userNode;
    }

    /**
     * @param objectMapper an instance of ObjectMapper used to create and manipulate JSON nodes
     * @return an ArrayNode representing the user's transactions
     */
    public ArrayNode transactionsTransformToArrayNode(final ObjectMapper objectMapper) {
        ArrayNode transactionsArray = objectMapper.createArrayNode();
        if (!transactions.isEmpty()) {
            for (Transaction transaction : transactions) {
                transactionsArray.add(transaction.transformToAnObjectNode(objectMapper));
            }
        }
        return transactionsArray;
    }
}
