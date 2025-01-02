package org.poo.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.transactions.Transaction;

public final class ClassicReport implements ReportGenerator {
    private final Bank bank;

    public ClassicReport(final Bank bank) {
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

        ObjectNode outputNode = mapper.createObjectNode();
        outputNode.put("IBAN", account.getIban());
        outputNode.put("balance", account.getBalance());
        outputNode.put("currency", account.getCurrency());

        // transactions output
        ArrayNode transactionsArray = mapper.createArrayNode();
        for (Transaction transaction : account.getTransactions()) {
            if (transaction.getTimestamp() >= startTimestamp
                    && transaction.getTimestamp() <= endTimestamp) {
                transactionsArray.add(transaction.transformToAnObjectNode(mapper));
            }
        }
        outputNode.set("transactions", transactionsArray);

        return outputNode;
    }
}
