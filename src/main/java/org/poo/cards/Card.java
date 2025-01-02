package org.poo.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;
import org.poo.transactions.Transaction;

@Getter
@Setter
public class Card {
    private String cardNumber;
    private boolean status; // active or inactive (frozen)
    private boolean frozen;
    private boolean warning;
    private double amount;
    private String currency;

    public Card(final String cardNumber) {
        this.cardNumber = cardNumber;
        this.status = true;
        this.frozen = false;
        this.warning = false;
    }

    /**
     * Freezes the card
     */
    public void freezeCard() {
        this.status = false;
        this.frozen = true;
    }

    /**
     * Sets the card status to warning
     */
    public void warnCard() {
        this.status = false;
        this.warning = true;
    }

    /**
     * This method is overridden in the OneTimeCard class
     * @param account the account of the user
     * @param user the user that owns the account
     * @param command the command input
     * @throws UnsupportedOperationException if the card does not support post payment
     */
    public void handlePostPayment(final Account account, final User user,
                                  final CommandInput command, final double convertedAmount) {
        Transaction transaction = new Transaction.TransactionBuilder()
                .setDescription("Card payment")
                .setTimestamp(command.getTimestamp())
                .setAmountPayOnline(convertedAmount)
                .setCommerciant(command.getCommerciant())
                .build();

        user.addTransaction(transaction);
        account.addTransaction(transaction);
    }

    /**
     * @param objectMapper the object mapper used to create the ObjectNode
     * @return ObjectNode representing the card
     */
    public ObjectNode transformToObjectNode(final ObjectMapper objectMapper) {
        ObjectNode cardNode = objectMapper.createObjectNode();

        cardNode.put("cardNumber", cardNumber);
        if (this.status) {
            cardNode.put("status", "active");
        } else {
            cardNode.put("status", "frozen");
        }
        return cardNode;
    }
}
