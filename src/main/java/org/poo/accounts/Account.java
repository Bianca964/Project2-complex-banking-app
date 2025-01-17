package org.poo.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.cards.Card;
import org.poo.cards.OneTimeCard;
import org.poo.transactions.Commerciant;
import org.poo.transactions.Transaction;
import org.poo.transactions.TransactionHistory;
import org.poo.users.User;

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
    //private ArrayList<Transaction> transactions;
    private TransactionHistory transactionHistory;
    private DiscountManager discountManager;

    // total amount spent by the account for the spending threshold commerciants
    private double totalAmountForSpendingThreshold;

    // the list of all commerciants to which the account has made transactions
    protected ArrayList<Commerciant> commerciants;


    public Account(final String currency, final String type, final int timestamp) {
        this.iban = generateIBAN();
        this.balance = 0;
        this.currency = currency;
        this.type = type;
        this.timestamp = timestamp;
        this.minBalance = 0;
        this.cards = new ArrayList<>();
        //this.transactions = new ArrayList<>();
        this.totalAmountForSpendingThreshold = 0;
        this.commerciants = new ArrayList<>();
        this.discountManager = new DiscountManager();
        this.transactionHistory = new TransactionHistory();
    }

    // DISCOUNTS

    // NrOfTransactions
    public boolean hasDiscountAvailable() {
        return this.discountManager.hasDiscountAvailable();
    }

    public void applyDiscount(Commerciant commerciant, double amountSpent) {
        discountManager.applyDiscount(commerciant, amountSpent, this);
    }

    public void setDiscountFood() {
        discountManager.setDiscountFood();
    }

    public void setDiscountClothes() {
        discountManager.setDiscountClothes();
    }

    public void setDiscountTech() {
        discountManager.setDiscountTech();
    }

    public boolean isDiscountFoodUsed() {
        return discountManager.isDiscountFoodUsed();
    }

    public boolean isDiscountClothesUsed() {
        return discountManager.isDiscountClothesUsed();
    }

    public boolean isDiscountTechUsed() {
        return discountManager.isDiscountTechUsed();
    }

    // SPENDING THRESHOLD
    public void applySpendingThresholdDiscount(User sender, double amountSpent) {
        discountManager.applySpendingThresholdDiscount(sender, amountSpent, this);
    }

    public Commerciant getCommerciant(final Commerciant wantedCommerciant) {
        for (Commerciant c : commerciants) {
            if (c.getName().equals(wantedCommerciant.getName())) {
                return c;
            }
        }
        return null;
    }

    public void addCommerciant(final Commerciant commerciant) {
        commerciants.add(commerciant);
    }

    public void incrementNrOfTrnscForCommerciant(final Commerciant commerciant) {
        if (commerciant != null) {
            commerciant.incrementNrTransactions();
        }
    }

    public void addAmountForSpendingThreshold(final double amount) {
        totalAmountForSpendingThreshold += amount;
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

    public void setAlias(final String alias, final User user) {
        this.alias = alias;
    }

    /**
     * Creates a one-time card with the given card number
     * @param cardNumber the card number of the one-time card to be created
     */
    public void createOneTimeCard(final String cardNumber, final User user) {
        OneTimeCard card = new OneTimeCard(cardNumber);
        addCard(card);
    }

    public void addCard(final Card card) {
        cards.add(card);
    }

    public Card getCardWithCardNumber(final String cardNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Creates a card with the given card number
     * @param cardNumber the card number of the card to be created
     */
    public void createCard(final String cardNumber, final User user) {
        Card card = new Card(cardNumber);
        addCard(card);
    }

    public void deleteCard(final Card card, final User user) {
        if (card != null) {
            cards.remove(card);
        }
    }

    /**
     * Increases the balance of the account
     * @param amount the amount to be deposited in the account
     */
    public void deposit(final double amount) {
        balance += amount;
    }

    public void addFunds(final double amount, final User user) {
        this.deposit(amount);
    }

    public abstract boolean isBusinessAccount();
    public abstract boolean isSavingAccount();
    public abstract boolean isClassicAccount();

    /**
     * Decreases the balance of the account
     * @param amount the amount to be withdrawn from the account
     */
    public void withdraw(final double amount) {
        balance -= amount;
    }

    /**
     * Adds a transaction to the account's list of transactions (ordered by timestamp)
     * @param transaction the transaction to be added to the account's list of transactions
     */
    public void addTransaction(final Transaction transaction) {
        transactionHistory.addTransaction(transaction);
    }

    public ArrayList<Transaction> getTransactions() {
        return transactionHistory.getTransactions();
    }

    /**
     * Abstract method that checks if the account supports reports
     * @return true if the account supports reports and false otherwise
     */
    public abstract boolean supportsReport();

    /**
     * Transforms the account to an ObjectNode
     * @param objectMapper the object mapper used to create the ObjectNode
     * @return ObjectNode representing the account
     */
    public ObjectNode transformToObjectNode(final ObjectMapper objectMapper) {
        ObjectNode accountNode = objectMapper.createObjectNode();
        accountNode.put("IBAN", iban);
        accountNode.put("balance", balance);
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
