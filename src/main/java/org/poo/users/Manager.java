package org.poo.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.fileio.UserInput;

public class Manager extends User {

    public Manager(final UserInput userInfo, final Bank bank) {
        super(userInfo, bank);
    }

    public Manager(final User user) {
        super(user.getUserInfo(), user.getServicePlan().getBank());
    }

    /**
     * Transforms the user's attributes into a JSON representation
     * @param objectMapper an instance of ObjectMapper used to create and manipulate JSON nodes
     * @return an ObjectNode representing the user's attributes
     */
    @Override
    public ObjectNode transformToAnObjectNode(final ObjectMapper objectMapper) {
        ObjectNode userNode = objectMapper.createObjectNode();

        userNode.put("username", getUsername());
        userNode.put("spent", getAmountSpentOnBusinessAccount());
        userNode.put("deposited", getAmountDepositedOnBusinessAccount());

        return userNode;
    }

}
