package org.poo.reports.businessreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

public final class TransactionReport extends BusinessReport {
    private final String type;

    public TransactionReport(final Bank bank) {
        super(bank);
        this.type = "transaction";
    }

    /**
     * Generates a "transaction" business report
     * @param commandInput the command input containing the necessary information
     * @param mapper the object mapper used to create the object node
     * @return the object node containing the whole report
     */
    @Override
    public ObjectNode generateReport(final CommandInput commandInput,
                                     final ObjectMapper mapper) throws Exception {
        BusinessAccount businessAccount;
        try {
            businessAccount = getBusinessAccount(commandInput);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        ObjectNode outputNode = generateCommonFields(mapper, businessAccount, type);

        outputNode.put("total spent", businessAccount.getTotalAmountSpent());
        outputNode.put("total deposited", businessAccount.getTotalAmountDeposited());

        // managers output
        ArrayNode managersArray = mapper.createArrayNode();
        for (User manager : businessAccount.getManagers()) {
            managersArray.add(manager.associateTransformToAnObjNode(mapper, businessAccount));
        }
        outputNode.set("managers", managersArray);

        // employees output
        ArrayNode employeesArray = mapper.createArrayNode();
        for (User employee : businessAccount.getEmployees()) {
            employeesArray.add(employee.associateTransformToAnObjNode(mapper, businessAccount));
        }
        outputNode.set("employees", employeesArray);

        return outputNode;
    }
}
