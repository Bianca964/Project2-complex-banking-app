package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Commerciant {
    private final String name;
    private double totalAmountSpent;

    public Commerciant(final String name, final double totalAmountSpent) {
        this.name = name;
        this.totalAmountSpent = totalAmountSpent;
    }

    /**
     * Add amount spent by the client to the total amount spent by the client at this commerciant
     * @param amount amount spent by the client (to be added to the total amount spent)
     */
    public void addAmountSpent(final double amount) {
        this.totalAmountSpent += amount;
    }

    /**
     * @param mapper ObjectMapper used to create the ObjectNode
     * @return ObjectNode representing the commerciant
     */
    public ObjectNode transformToObjectNode(final ObjectMapper mapper) {
        ObjectNode commerciantNode = mapper.createObjectNode();

        commerciantNode.put("commerciant", name);
        commerciantNode.put("total", totalAmountSpent);

        return commerciantNode;
    }
}
