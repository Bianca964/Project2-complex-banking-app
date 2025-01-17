package org.poo.commands.accountcommands.savingsaccountcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;

public final class AddInterestCommand extends Command {

    public AddInterestCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        try {
            bank.addInterest(commandInput.getAccount(), commandInput.getTimestamp());
        } catch (Exception e) {
            addCommandAndTimestamp(objectNode);

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", e.getMessage());
            outputNode.put("timestamp", commandInput.getTimestamp());
            objectNode.set("output", outputNode);

        }
    }
}
