package org.poo.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.cards.Card;
import org.poo.fileio.UserInput;

import java.util.ArrayList;

public class Employee extends User {
    private ArrayList<Card> cardsAddedToBusinessAccount;

    public Employee(final UserInput userInfo, final Bank bank) {
        super(userInfo, bank);
        this.cardsAddedToBusinessAccount = new ArrayList<>();
    }

    public Employee(final User user) {
        super(user.getUserInfo(), user.getServicePlan().getBank());
        this.cardsAddedToBusinessAccount = new ArrayList<>();
    }

    public void addCardToCardsAddedToBusinessAccount(final Card card) {
        cardsAddedToBusinessAccount.add(card);
    }

    public void removeCardFromCardsAddedToBusinessAccount(final Card card) {
        cardsAddedToBusinessAccount.remove(card);
    }

    public boolean hasCard(final Card card) {
        return cardsAddedToBusinessAccount.contains(card);
    }



    /**
     * Transforms the user's attributes into a JSON representation
     * @param objectMapper an instance of ObjectMapper used to create and manipulate JSON nodes
     * @return an ObjectNode representing the user's attributes
     */
    public ObjectNode transformToAnObjectNode(final ObjectMapper objectMapper) {
        ObjectNode userNode = objectMapper.createObjectNode();

        userNode.put("username", getUsername());
        userNode.put("spent", getAmountSpentOnBusinessAccount());
        userNode.put("deposited", getAmountDepositedOnBusinessAccount());

        return userNode;
    }




}
