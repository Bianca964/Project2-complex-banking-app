package org.poo.reports.businessreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;

public final class CommerciantReport extends BusinessReport {
    private final String type;

    public CommerciantReport(final Bank bank) {
        super(bank);
        this.type = "commerciant";
    }

    /**
     * Generates a "commerciant" business report
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

        ArrayNode commerciantsArray = mapper.createArrayNode();
        for (Commerciant commerciant : businessAccount.getCommerciantsAddedByAssociates()) {
            commerciantsArray.add(commerciant.transformToObjectNodeForBusinessReport(mapper));
        }
        outputNode.set("commerciants", commerciantsArray);

        return outputNode;
    }
}
