package org.poo.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.cards.Card;
import org.poo.cards.OneTimeCard;
import org.poo.transactions.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static org.poo.utils.Utils.generateIBAN;

@Getter
@Setter
public abstract class Account {
    private String iban;
    private double balance;
    private String currency;
    private String type;
    private ArrayList<Card> cards;
    private int timestamp;
    private String alias;
    private double minBalance;
    private ArrayList<Transaction> transactions;

    public Account(final String currency, final String type, final int timestamp) {
        this.iban = generateIBAN();
        this.balance = 0;
        this.currency = currency;
        this.type = type;
        this.timestamp = timestamp;
        this.minBalance = 0;
        this.cards = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    /**
     * @param cardNumber the card number of the card to be returned from this account
     * @return the card with the given card number
     */
    public Card getCard(final String cardNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Checks if the account has enough balance for a given amount
     * @param amount the amount to be compared with the account's balance
     * @return true if the account has enough balance
     */
    public boolean hasEnoughBalance(final double amount) {
        return balance >= amount;
    }

    /**
     * @return true if the account has money in it
     */
    public boolean hasMoneyInAccount() {
        return balance > 0;
    }

    /**
     * Sets the minimum balance for the account
     * @param argMinBalance the minimum balance to be set for the account
     */
    public void setMinimumBalance(final double argMinBalance) {
        this.minBalance = argMinBalance;
    }

    /**
     * Creates a one-time card with the given card number
     * @param cardNumber the card number of the one-time card to be created
     */
    public void createOneTimeCard(final String cardNumber) {
        OneTimeCard card = new OneTimeCard(cardNumber);
        cards.add(card);
    }

    /**
     * Creates a card with the given card number
     * @param cardNumber the card number of the card to be created
     */
    public void createCard(final String cardNumber) {
        Card card = new Card(cardNumber);
        cards.add(card);
    }

    /**
     * Increases the balance of the account
     * @param amount the amount to be deposited in the account
     */
    public void deposit(final double amount) {
        balance += amount;
    }

    /**
     * Decreases the balance of the account
     * @param amount the amount to be withdrawn from the account
     */
    public void withdraw(final double amount) {
        balance -= amount;
    }

    /**
     * Adds a transaction to the account's list of transactions
     * @param transaction the transaction to be added to the account's list of transactions
     */
    public void addTransaction(final Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Abstract method that checks if the account supports reports
     * @return true if the account supports reports and false otherwise
     */
    public abstract boolean supportsReport();

    /**
     * Abstract method that checks if the account has interest
     * @return true if the account has interest and false otherwise
     */
    public abstract boolean hasInterest();

    /**
     * Transforms the account to an ObjectNode
     * @param objectMapper the object mapper used to create the ObjectNode
     * @return ObjectNode representing the account
     */
    public ObjectNode transformToObjectNode(final ObjectMapper objectMapper) {
        // Round to 2 decimal places
        BigDecimal roundedBalance = new BigDecimal(balance).setScale(2, RoundingMode.HALF_UP);

        ObjectNode accountNode = objectMapper.createObjectNode();
        accountNode.put("IBAN", iban);
        accountNode.put("balance", roundedBalance.doubleValue());
        accountNode.put("currency", currency);
        accountNode.put("type", type);

        ArrayNode cardsArray = objectMapper.createArrayNode();
        if (cards != null && !cards.isEmpty()) {
            for (Card card : cards) {
                cardsArray.add(card.transformToObjectNode(objectMapper));
            }
        }
        accountNode.set("cards", cardsArray);

        return accountNode;
    }
}
