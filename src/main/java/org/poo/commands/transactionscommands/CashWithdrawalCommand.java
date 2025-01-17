package org.poo.commands.transactionscommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionService;

public class CashWithdrawalCommand extends Command {
    public CashWithdrawalCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        try {
            TransactionService transactionService = new TransactionService(bank);
            transactionService.cashWithdrawal(commandInput);
        } catch (Exception e) {
            addCommandAndTimestamp(objectNode);

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", e.getMessage());
            outputNode.put("timestamp", commandInput.getTimestamp());
            objectNode.set("output", outputNode);
        }
    }
}
