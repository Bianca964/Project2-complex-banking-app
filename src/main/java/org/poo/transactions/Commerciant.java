package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;

@Getter
@Setter
public class Commerciant {
    private String name;
    private int id;
    private String account;
    private String type;
    private String cashbackStrategy;
    private double totalAmountSpent;

    @Setter
    @Getter
    private int nrTransactions;

    public Commerciant(CommerciantInput commerciantInput) {
        this.name = commerciantInput.getCommerciant();
        this.id = commerciantInput.getId();
        this.account = commerciantInput.getAccount();
        this.type = commerciantInput.getType();
        this.cashbackStrategy = commerciantInput.getCashbackStrategy();
        this.totalAmountSpent = 0;
        this.nrTransactions = 0;
    }

    public Commerciant(Commerciant commerciant, double totalAmountSpent) {
        this.name = commerciant.getName();
        this.id = commerciant.getId();
        this.account = commerciant.getAccount();
        this.type = commerciant.getType();
        this.cashbackStrategy = commerciant.getCashbackStrategy();
        this.totalAmountSpent = totalAmountSpent;
    }

    public void incrementNrTransactions() {
        this.nrTransactions++;
    }

    public boolean isNrOfTransactionType() {
        return cashbackStrategy.equals("nrOfTransactions");
    }

    public boolean isSpendingThresholdType() {
        return cashbackStrategy.equals("spendingThreshold");
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
