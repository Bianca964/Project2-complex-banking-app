package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.accounts.Account;
import org.poo.accounts.ClassicAccount;
import org.poo.fileio.CommerciantInput;

@Getter
@Setter
public class Commerciant {
    private String name;
    private int id;
    private String accountIban;
    private String type;
    private String cashbackStrategy;
    private double totalAmountSpent;
    private int nrTransactions;

    public Commerciant(CommerciantInput commerciantInput) {
        this.name = commerciantInput.getCommerciant();
        this.id = commerciantInput.getId();
        this.accountIban = commerciantInput.getAccount();
        this.type = commerciantInput.getType();
        this.cashbackStrategy = commerciantInput.getCashbackStrategy();
        this.totalAmountSpent = 0;
        this.nrTransactions = 0;
    }

    public Commerciant(Commerciant commerciant, double totalAmountSpent) {
        this.name = commerciant.getName();
        this.id = commerciant.getId();
        this.accountIban = commerciant.getAccountIban();
        this.type = commerciant.getType();
        this.cashbackStrategy = commerciant.getCashbackStrategy();
        this.totalAmountSpent = totalAmountSpent;

        // I do not need these when I construct commerciant with this constructor
        this.nrTransactions = 0;
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
