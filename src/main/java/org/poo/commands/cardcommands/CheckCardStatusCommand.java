package org.poo.commands.cardcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;

public final class CheckCardStatusCommand extends Command {

    public CheckCardStatusCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        try {
            bank.checkCardStatus(commandInput.getCardNumber(), commandInput.getTimestamp());
        } catch (Exception e) {
            addCommandAndTimestamp(objectNode);

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("timestamp", commandInput.getTimestamp());
            outputNode.put("description", e.getMessage());
            objectNode.set("output", outputNode);
        }
    }
}
