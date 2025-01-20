package org.poo.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.cards.Card;
import org.poo.cards.OneTimeCard;
import org.poo.commerciants.Commerciant;
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
        this.totalAmountForSpendingThreshold = 0;
        this.commerciants = new ArrayList<>();
        this.discountManager = new DiscountManager();
        this.transactionHistory = new TransactionHistory();
    }

    /**
     * @return true if the account has a discount available
     */
    public boolean hasDiscountAvailable() {
        return this.discountManager.hasDiscountAvailable();
    }

    /**
     * Applies a discount to the account according to the commerciant
     * @param commerciant the commerciant to which the discount is applied
     * @param amountSpent the amount spent at the commerciant in the current transaction
     */
    public void applyDiscount(final Commerciant commerciant, final double amountSpent) {
        discountManager.applyDiscount(commerciant, amountSpent, this);
    }

    /**
     * Sets the discount for food available
     */
    public void setDiscountFood() {
        discountManager.setDiscountFood();
    }

    /**
     * Sets the discount for clothes available
     */
    public void setDiscountClothes() {
        discountManager.setDiscountClothes();
    }

    /**
     * Sets the discount for tech available
     */
    public void setDiscountTech() {
        discountManager.setDiscountTech();
    }

    /**
     * @return true if the discount for food was used
     */
    public boolean isDiscountFoodUsed() {
        return discountManager.isDiscountFoodUsed();
    }

    /**
     * @return true if the discount for clothes was used
     */
    public boolean isDiscountClothesUsed() {
        return discountManager.isDiscountClothesUsed();
    }

    /**
     * @return true if the discount for tech was used
     */
    public boolean isDiscountTechUsed() {
        return discountManager.isDiscountTechUsed();
    }

    /**
     * Applies a spending threshold discount to the account according to the amount spent
     * @param sender the user who made the transaction
     * @param amountSpent the amount spent in the current transaction
     */
    public void applySpendingThresholdDiscount(final User sender, final double amountSpent) {
        discountManager.applySpendingThresholdDiscount(sender, amountSpent, this);
    }

    /**
     * Gets a commerciant from the list of commerciants to which the account has made transactions
     * @param wantedCommerciant the commerciant to be returned
     * @return the commerciant wanted from the list of commerciants of the account
     */
    public Commerciant getCommerciant(final Commerciant wantedCommerciant) {
        for (Commerciant c : commerciants) {
            if (c.getName().equals(wantedCommerciant.getName())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Checks if the account has a commerciant in its list of commerciants
     * @param wantedCommerciant the commerciant to be checked
     * @return true if the account has the commerciant in its list of commerciants, false otherwise
     */
    public boolean hasCommerciant(final Commerciant wantedCommerciant) {
        for (Commerciant c : commerciants) {
            if (c.getName().equals(wantedCommerciant.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a commerciant to the list of commerciants of the account
     * @param commerciant the commerciant to be added to the list of commerciants to
     *                    which the account has made transactions
     */
    public void addCommerciant(final Commerciant commerciant) {
        commerciants.add(commerciant);
    }

    /**
     * Increments the number of transactions made to a commerciant
     * @param commerciant the commerciant for which the number of transactions is incremented
     */
    public void incrementNrOfTrnscForCommerciant(final Commerciant commerciant) {
        if (commerciant != null) {
            commerciant.incrementNrTransactions();
        }
    }

    /**
     * Adds an amount to the total amount spent by the account for the spending
     * threshold commerciants
     * @param amount the amount to be added to the total amount spent by the account
     */
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

    /**
     * Sets the alias for the account
     * @param newAlias the alias to be set for the account
     * @param user the user who sets the alias
     */
    public void setAlias(final String newAlias, final User user) {
        this.alias = newAlias;
    }

    /**
     * Creates a one-time card with the given card number
     * @param cardNumber the card number of the one-time card to be created
     * @param user the user who creates the one-time card
     */
    public void createOneTimeCard(final String cardNumber, final User user) throws Exception {
        OneTimeCard card = new OneTimeCard(cardNumber);
        addCard(card);
    }

    /**
     * Adds a card to the account
     * @param card the card to be added to the account
     */
    public void addCard(final Card card) {
        cards.add(card);
    }

    /**
     * @param cardNumber the card number of the card to be returned
     * @return the card with the given card number from this account
     */
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
     * @param user the user who creates the card
     */
    public void createCard(final String cardNumber, final User user) throws Exception {
        Card card = new Card(cardNumber);
        addCard(card);
    }

    /**
     * Deletes a card from the account
     * @param card the card to be deleted from the account
     * @param user the user who deletes the card
     */
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

    /**
     * Decreases the balance of the account
     * @param amount the amount to be withdrawn from the account
     */
    public void withdraw(final double amount) {
        balance -= amount;
    }

    /**
     * Adds funds to the account
     * @param amount the amount to be added to the account
     * @param user the user who adds the funds
     */
    public void addFunds(final double amount, final User user) {
        this.deposit(amount);
    }

    /**
     * @return true if the account is a business account and false otherwise
     */
    public abstract boolean isBusinessAccount();

    /**
     * @return true if the account is a saving account and false otherwise
     */
    public abstract boolean isSavingAccount();

    /**
     * @return true if the account is a classic account and false otherwise
     */
    public abstract boolean isClassicAccount();

    /**
     * Adds a transaction to the account's list of transactions (ordered by timestamp)
     * @param transaction the transaction to be added to the account's list of transactions
     */
    public void addTransaction(final Transaction transaction) {
        transactionHistory.addTransaction(transaction);
    }

    /**
     * @return the list of transactions of the account
     */
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
