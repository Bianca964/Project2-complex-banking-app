package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.users.User;
import org.poo.fileio.CommandInput;

public class WithdrawSavingsCommand extends Command {

    public WithdrawSavingsCommand(CommandInput commandInput, ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(Bank bank, ObjectNode objectNode) {
        User user = bank.getUserWithAccount(commandInput.getAccount());

        if (user != null) {
            try {
                bank.withdrawSavings(commandInput);
            } catch (Exception e) {
//                addCommandAndTimestamp(objectNode);
//                ObjectNode outputNode = mapper.createObjectNode();
//                outputNode.put("error", e.getMessage());
//                objectNode.set("output", outputNode);
            }
        }
    }
}
