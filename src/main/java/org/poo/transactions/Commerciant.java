package org.poo.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;
import org.poo.users.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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


    public Commerciant(CommerciantInput commerciantInput) {
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

    public Commerciant(Commerciant commerciant, double totalAmountSpent) {
        this.name = commerciant.getName();
        this.id = commerciant.getId();
        this.accountIban = commerciant.getAccountIban();
        this.type = commerciant.getType();
        this.cashbackStrategy = commerciant.getCashbackStrategy();
        this.totalAmountSpent = totalAmountSpent;
        this.totalAmountReceived = 0;
        this.managers = new ArrayList<>();
        this.employees = new ArrayList<>();

        // I do not need these when I construct commerciant with this constructor
        this.nrTransactions = 0;
    }





    // tb sa i adaug in ordine alfabetica dupa numele managerului
    public void addManager(final User manager) {
        this.managers.add(manager);
        managers.sort(Comparator.comparing(User::getUsername));
    }

    // tb sa i adaug in ordine alfabetica dupa numele angajatului
    public void addEmployee(final User employee) {
        this.employees.add(employee);
        employees.sort(Comparator.comparing(User::getUsername));
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

    public void addAmountReceived(final double amount) {
        this.totalAmountReceived += amount;
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
    public ObjectNode transformToObjectNodeForSpendingsReport(final ObjectMapper mapper) {
        ObjectNode commerciantNode = mapper.createObjectNode();

        commerciantNode.put("commerciant", name);
        commerciantNode.put("total", totalAmountSpent);

        return commerciantNode;
    }


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
