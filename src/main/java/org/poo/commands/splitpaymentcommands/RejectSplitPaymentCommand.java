package org.poo.commands.splitpaymentcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;
import org.poo.transactions.SplitPayment;
import org.poo.transactions.Transaction;
import org.poo.users.User;

public class RejectSplitPaymentCommand extends Command {
    public RejectSplitPaymentCommand(CommandInput commandInput, ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(Bank bank, ObjectNode objectNode) {
        User user = bank.getUserWithEmail(commandInput.getEmail());
        if (user == null) {
            addCommandAndTimestamp(objectNode);
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", commandInput.getTimestamp());
            objectNode.set("output", outputNode);
            return;
        }
        // get the splitPayment that the user wants to reject
        SplitPayment splitPayment = user.getFirstSplitTransactionOfType(commandInput.getSplitPaymentType());

        // if someone rejects, remove the splitPayment from any user that was involved in it and add the transaction to all users
        if (splitPayment != null) {
            // acest splitpayment tb sa l sterg si din toti ceilalti useri

            Transaction transaction = splitPayment.createTransactionForReject();
            for (User userFromSplitPayment : splitPayment.getUsers()) {
                userFromSplitPayment.removeSplitPayment(splitPayment);
                userFromSplitPayment.addTransaction(transaction);
            }
        }

    }
}
