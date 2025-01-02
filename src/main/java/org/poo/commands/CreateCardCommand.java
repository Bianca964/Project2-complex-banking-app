package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

public final class CreateCardCommand extends Command {
    public CreateCardCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        User user = bank.getUserWithEmail(commandInput.getEmail());
        if (user != null) {
            user.createCard(commandInput.getAccount(), commandInput.getTimestamp());
        }
    }
}
