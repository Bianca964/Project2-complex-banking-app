package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;

public abstract class Command {
    protected CommandInput commandInput;
    protected ObjectMapper mapper;

    public Command(final CommandInput commandInput, final ObjectMapper mapper) {
        this.commandInput = commandInput;
        this.mapper = mapper;
    }

    /**
     * Executes the command
     * @param bank the bank object
     * @param objectNode the object node to be filled with the command result
     */
    public abstract void execute(Bank bank, ObjectNode objectNode);

    /**
     * Adds the command and timestamp to the object node
     * @param objectNode the object node to be filled with the command and timestamp
     */
    protected void addCommandAndTimestamp(final ObjectNode objectNode) {
        objectNode.put("command", commandInput.getCommand());
        objectNode.put("timestamp", commandInput.getTimestamp());
    }
}
