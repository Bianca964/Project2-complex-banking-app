package org.poo.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.transactions.Commerciant;
import org.poo.users.User;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BusinessReport implements ReportGenerator {
    private final Bank bank;

    public BusinessReport(final Bank bank) {
        this.bank = bank;
    }

    @Override
    public ObjectNode generateReport(final CommandInput commandInput,
                                     final ObjectMapper mapper) throws Exception {

        int startTimestamp = commandInput.getStartTimestamp();
        int endTimestamp = commandInput.getEndTimestamp();

        Account account = bank.getAccountWithIBAN(commandInput.getAccount());
        if (account == null) {
            throw new Exception("Account not found");
        }

        BusinessAccount businessAccount;
        if (account.isBusinessAccount()) {
            businessAccount = (BusinessAccount) account;
        } else {
            throw new Exception("Account is not a business account");
        }

        String type = commandInput.getType();

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("IBAN", businessAccount.getIban());
        outputNode.put("balance", businessAccount.getBalance());
        outputNode.put("currency", businessAccount.getCurrency());
        outputNode.put("spending limit", businessAccount.getSpendingLimitForEmployees());
        outputNode.put("deposit limit", businessAccount.getDepositLimit());
        outputNode.put("statistics type", type);

        if (type.equals("commerciant")) {
            ArrayNode commerciantsArray = mapper.createArrayNode();
            for (Commerciant commerciant : businessAccount.getCommerciantsAddedByAssociates()) {
                commerciantsArray.add(commerciant.transformToObjectNodeForBusinessReport(mapper));
            }
            outputNode.set("commerciants", commerciantsArray);
        }

        if (type.equals("transaction")) {
            outputNode.put("total spent", businessAccount.getTotalAmountSpent());
            outputNode.put("total deposited", businessAccount.getTotalAmountDeposited());

            // managers output
            ArrayNode managersArray = mapper.createArrayNode();
            for (User manager : businessAccount.getManagers()) {
                managersArray.add(manager.associateTransformToAnObjectNode(mapper, businessAccount));
            }
            outputNode.set("managers", managersArray);

            // employees output
            ArrayNode employeesArray = mapper.createArrayNode();
            for (User employee : businessAccount.getEmployees()) {
                employeesArray.add(employee.associateTransformToAnObjectNode(mapper, businessAccount));
            }
            outputNode.set("employees", employeesArray);
        }

        return outputNode;
    }
}
