package org.poo.bank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.poo.commands.Command;
import org.poo.commands.CommandFactory;
import org.poo.fileio.CommandInput;
import org.poo.fileio.ObjectInput;

import static org.poo.utils.Utils.resetRandom;

public final class InitBank {
    private final ObjectMapper mapper;
    @Getter
    private final ArrayNode outputArray;
    private final Bank bank;
    private final CommandFactory commandFactory;

    public InitBank(final ObjectInput input) {
        this.mapper = new ObjectMapper();
        this.outputArray = this.mapper.createArrayNode();
        this.bank = Bank.getInstance(input.getUsers(), input.getExchangeRates(), input.getCommerciants());
        this.commandFactory = new CommandFactory(mapper);
    }

    /**
     * @param commandInput the object with the whole input
     * @param objectNode the object node to be filled with the command result
     */
    public void processCommand(final CommandInput commandInput, final ObjectNode objectNode) {
        Command command = commandFactory.createCommand(commandInput);
        if (command != null) {
            command.execute(bank, objectNode);
        }
    }

    /**
     * Processes all commands from the input
     * @param input the input object containing all commands
     */
    public void run(final ObjectInput input) {
        resetRandom();
        if (input.getCommands() != null) {
            for (int i = 0; i < input.getCommands().length; i++) {
                ObjectNode objectNode = this.mapper.createObjectNode();

                processCommand(input.getCommands()[i], objectNode);

                if (!objectNode.isEmpty()) {
                    this.outputArray.add(objectNode);
                }
            }
        }
        Bank.resetBank();
    }
}
