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
    private ArrayList<Transaction> transactions;

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
        this.transactions = new ArrayList<>();
        this.totalAmountForSpendingThreshold = 0;
        this.commerciants = new ArrayList<>();
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

    /**
     * Creates a one-time card with the given card number
     * @param cardNumber the card number of the one-time card to be created
     */
    public void createOneTimeCard(final String cardNumber) {
        OneTimeCard card = new OneTimeCard(cardNumber);
        cards.add(card);
    }

    public void addCard(final Card card) {
        cards.add(card);
    }

    /**
     * Creates a card with the given card number
     * @param cardNumber the card number of the card to be created
     */
    public void createCard(final String cardNumber, final User user) {
        Card card = new Card(cardNumber);
        addCard(card);
    }

    /**
     * Increases the balance of the account
     * @param amount the amount to be deposited in the account
     */
    public void deposit(final double amount) {
        balance += amount;
    }





    public void addFunds(final double amount, final User user) throws Exception{
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
        int index = 0;
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getTimestamp() > transaction.getTimestamp()) {
                index = i;
                break;
            }
            index = i + 1; // Dacă ajunge la final, se inserează la sfârșit
        }
        transactions.add(index, transaction); // Inserăm tranzacția la poziția calculat
    }

    /**
     * Abstract method that checks if the account supports reports
     * @return true if the account supports reports and false otherwise
     */
    public abstract boolean supportsReport();

//    /**
//     * Abstract method that checks if the account has interest
//     * @return true if the account has interest and false otherwise
//     */
//    public abstract boolean hasInterest();

    /**
     * Transforms the account to an ObjectNode
     * @param objectMapper the object mapper used to create the ObjectNode
     * @return ObjectNode representing the account
     */
    public ObjectNode transformToObjectNode(final ObjectMapper objectMapper) {
        // Round to 2 decimal places
        //BigDecimal roundedBalance = new BigDecimal(balance).setScale(2, RoundingMode.HALF_UP);

        ObjectNode accountNode = objectMapper.createObjectNode();
        accountNode.put("IBAN", iban);
        accountNode.put("balance", balance);
        accountNode.put("currency", currency);
        accountNode.put("type", type);




        // update the account's balance
        //this.setBalance(roundedBalance.doubleValue());



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
