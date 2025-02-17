package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.transactions.TransactionService;

public final class SplitPaymentCommand extends Command {

    public SplitPaymentCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        TransactionService transactionService = new TransactionService(bank);
        transactionService.splitPayment(commandInput);
    }
}
