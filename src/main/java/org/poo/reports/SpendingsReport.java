package org.poo.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.commerciants.Commerciant;
import org.poo.transactions.Transaction;

import java.util.TreeMap;

public final class SpendingsReport implements ReportGenerator {
    private final Bank bank;

    public SpendingsReport(final Bank bank) {
        this.bank = bank;
    }

    @Override
    public ObjectNode generateReport(final CommandInput commandInput,
                                     final ObjectMapper mapper) throws Exception {
        int startTimestamp = commandInput.getStartTimestamp();
        int endTimestamp = commandInput.getEndTimestamp();
        Account account = bank.getAccountWithIBAN(commandInput.getAccount());

        // TreeMap for keeping commerciants ordered alphabetically by name
        TreeMap<String, Commerciant> commerciantsMap = new TreeMap<>();

        if (account == null) {
            throw new Exception("Account not found");
        }
        if (!account.supportsReport()) {
            throw new Exception("This kind of report is not supported for a saving account");
        }

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("IBAN", account.getIban());
        outputNode.put("balance", account.getBalance());
        outputNode.put("currency", account.getCurrency());

        // transaction output
        ArrayNode transactionsArray = mapper.createArrayNode();
        for (Transaction transaction : account.getTransactions()) {
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp
                    && transaction.getDescription().equals("Card payment")) {
                transactionsArray.add(transaction.transformToAnObjectNode(mapper));

                // data for commerciant
                String commerciantName = transaction.getCommerciant();
                double amount = transaction.getAmountPayOnline();

                Commerciant oldCommerciant = bank.getCommerciantWithName(commerciantName);
                if (oldCommerciant == null) {
                    throw new Exception("Commerciant not found");
                }

                // add only the used commerciants
                Commerciant commerciant = commerciantsMap.get(commerciantName);
                if (commerciant == null) {
                    commerciant = new Commerciant(oldCommerciant, amount);
                    commerciantsMap.put(commerciantName, commerciant);
                } else {
                    commerciant.addAmountSpent(amount);
                }
            }
        }
        outputNode.set("transactions", transactionsArray);

        // commerciants output
        ArrayNode commerciantsArray = mapper.createArrayNode();
        for (Commerciant commerciant : commerciantsMap.values()) {
            commerciantsArray.add(commerciant.transformToObjectNodeForSpendingsReport(mapper));
        }
        outputNode.set("commerciants", commerciantsArray);

        return outputNode;
    }
}
