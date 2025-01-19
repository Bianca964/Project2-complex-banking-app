package org.poo.reports.businessreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.reports.ReportGenerator;

public abstract class BusinessReport implements ReportGenerator {
    protected final Bank bank;

    public BusinessReport(final Bank bank) {
        this.bank = bank;
    }

    /**
     * Abstract method for generating a business report which can be of two types:
     * "commerciant" or "transaction"
     * @param commandInput the command input containing the necessary information
     * @param mapper the object mapper used to create the object node
     * @return the object node containing the report
     * @throws Exception if any errors occurs
     */
    public abstract ObjectNode generateReport(CommandInput commandInput,
                                              ObjectMapper mapper) throws Exception;

    /**
     * Gets the business account from the bank
     * @param commandInput the command input
     * @return the business account for which the report is generated
     * @throws Exception if any errors occurs
     */
    protected BusinessAccount getBusinessAccount(final CommandInput commandInput) throws Exception {
        Account account = bank.getAccountWithIBAN(commandInput.getAccount());
        if (account == null) {
            throw new Exception("Account not found");
        }

        if (account.isBusinessAccount()) {
            return ((BusinessAccount) account);
        } else {
            throw new Exception("Account is not a business account");
        }
    }

    /**
     * Generates the common fields for "commerciant" and "transaction" reports
     * @param mapper the object mapper used to create the object node
     * @param businessAccount the business account for which the report is made
     * @param type the type of the report
     * @return the object node containing the common fields
     */
    protected ObjectNode generateCommonFields(final ObjectMapper mapper,
                                              final BusinessAccount businessAccount,
                                              final String type) {
        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("IBAN", businessAccount.getIban());
        outputNode.put("balance", businessAccount.getBalance());
        outputNode.put("currency", businessAccount.getCurrency());
        outputNode.put("spending limit", businessAccount.getSpendingLimit());
        outputNode.put("deposit limit", businessAccount.getDepositLimit());
        outputNode.put("statistics type", type);

        return outputNode;
    }
}
