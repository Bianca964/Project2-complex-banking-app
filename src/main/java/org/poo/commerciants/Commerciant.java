package org.poo.commerciants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;
import org.poo.users.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
    private double totalAmountReceived;
    private ArrayList<User> managers;
    private ArrayList<User> employees;

    public Commerciant(final CommerciantInput commerciantInput) {
        this.name = commerciantInput.getCommerciant();
        this.id = commerciantInput.getId();
        this.accountIban = commerciantInput.getAccount();
        this.type = commerciantInput.getType();
        this.cashbackStrategy = commerciantInput.getCashbackStrategy();
        this.totalAmountSpent = 0;
        this.nrTransactions = 0;
        this.totalAmountReceived = 0;
        this.managers = new ArrayList<>();
        this.employees = new ArrayList<>();
    }

    public Commerciant(final Commerciant commerciant, final double totalAmountSpent) {
        this.name = commerciant.getName();
        this.id = commerciant.getId();
        this.accountIban = commerciant.getAccountIban();
        this.type = commerciant.getType();
        this.cashbackStrategy = commerciant.getCashbackStrategy();
        this.totalAmountSpent = totalAmountSpent;
        this.totalAmountReceived = 0;
        this.managers = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.nrTransactions = 0;
    }

    /**
     * Add manager to the list of managers ordered alphabetically by the manager's name
     * @param manager manager to be added to the list of managers
     */
    public void addManager(final User manager) {
        int index = Collections.binarySearch(managers, manager,
                                             Comparator.comparing(User::getUsername));
        if (index < 0) {
            index = -(index + 1);
        }
        managers.add(index, manager);
    }

    /**
     * Add employee to the list of employees ordered alphabetically by the employee's name
     * @param employee employee to be added to the list of employees
     */
    public void addEmployee(final User employee) {
        int index = Collections.binarySearch(employees, employee,
                                             Comparator.comparing(User::getUsername));
        if (index < 0) {
            index = -(index + 1);
        }
        employees.add(index, employee);
    }

    /**
     * Increment the number of transactions made by the client at this commerciant
     */
    public void incrementNrTransactions() {
        this.nrTransactions++;
    }

    /**
     * @return true if the cashback strategy is "nrOfTransactions", false otherwise
     */
    public boolean isNrOfTransactionType() {
        return cashbackStrategy.equals("nrOfTransactions");
    }

    /**
     * @return true if the cashback strategy is "spendingThreshold", false otherwise
     */
    public boolean isSpendingThresholdType() {
        return cashbackStrategy.equals("spendingThreshold");
    }

    /**
     * Add to the total amount received by this commerciant
     * @param amount received by the commerciant (paid by the client)
     */
    public void addAmountReceived(final double amount) {
        this.totalAmountReceived += amount;
    }

    /**
     * Add amount spent by the clients to the total amount spent at this commerciant
     * @param amount amount spent by the client
     */
    public void addAmountSpent(final double amount) {
        this.totalAmountSpent += amount;
    }

    /**
     * @param mapper ObjectMapper used to create the ObjectNode
     * @return ObjectNode representing the commerciant
     */
    public ObjectNode transformToObjectNodeForSpendingsReport(final ObjectMapper mapper) {
        ObjectNode commerciantNode = mapper.createObjectNode();

        commerciantNode.put("commerciant", name);
        commerciantNode.put("total", totalAmountSpent);

        return commerciantNode;
    }

    /**
     * @param mapper ObjectMapper used to create the ObjectNode
     * @return ObjectNode representing the commerciant
     */
    public ObjectNode transformToObjectNodeForBusinessReport(final ObjectMapper mapper) {
        ObjectNode commerciantNode = mapper.createObjectNode();

        commerciantNode.put("commerciant", name);
        commerciantNode.put("total received", totalAmountReceived);

        // add managers
        ArrayNode managersArray = mapper.createArrayNode();
        for (User manager : managers) {
            managersArray.add(manager.getUsername());
        }
        commerciantNode.set("managers", managersArray);

        // add employees
        ArrayNode employeesArray = mapper.createArrayNode();
        for (User employee : employees) {
            employeesArray.add(employee.getUsername());
        }
        commerciantNode.set("employees", employeesArray);

        return commerciantNode;
    }
}
