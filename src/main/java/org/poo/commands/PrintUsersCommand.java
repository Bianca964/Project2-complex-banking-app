package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;

public final class PrintUsersCommand extends Command {

    public PrintUsersCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        addCommandAndTimestamp(objectNode);
        objectNode.set("output", bank.usersTransformToArrayNode(mapper));
    }
}
