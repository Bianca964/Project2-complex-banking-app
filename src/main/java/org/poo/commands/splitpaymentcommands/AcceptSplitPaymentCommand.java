package org.poo.commands.splitpaymentcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.users.User;
import org.poo.fileio.CommandInput;
import org.poo.transactions.SplitPayment;

public class AcceptSplitPaymentCommand extends Command {
    public AcceptSplitPaymentCommand(CommandInput commandInput, ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(Bank bank, ObjectNode objectNode) {

        // after every accept, verify if the transaction can be executed (has all accepts)
        User user = bank.getUserWithEmail(commandInput.getEmail());
        if (user == null) {
            addCommandAndTimestamp(objectNode);
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", "User not found");
            outputNode.put("timestamp", commandInput.getTimestamp());
            objectNode.set("output", outputNode);
            return;
        }
        SplitPayment splitPayment = user.acceptSplitPayment(commandInput.getSplitPaymentType());

        // execute split payment only if everyone has accepted
        if (splitPayment != null) {
            if (splitPayment.everyoneHasAccepted()) {
                splitPayment.execute();
            }
        }

    }
}
