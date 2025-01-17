package org.poo.commands.accountcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.users.User;
import org.poo.fileio.CommandInput;

public final class DeleteAccountCommand extends Command {

    public DeleteAccountCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        User userToDeleteAccount = bank.getUserWithEmail(commandInput.getEmail());
        if (userToDeleteAccount != null) {
            try {
                userToDeleteAccount.deleteAccount(commandInput.getAccount(),
                        commandInput.getTimestamp());
            } catch (Exception e) {
                addCommandAndTimestamp(objectNode);
                ObjectNode outputNode = mapper.createObjectNode();
                outputNode.put("error", e.getMessage());
                outputNode.put("timestamp", commandInput.getTimestamp());
                objectNode.set("output", outputNode);

                return;
            }

            addCommandAndTimestamp(objectNode);

            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("success", "Account deleted");
            outputNode.put("timestamp", commandInput.getTimestamp());
            objectNode.set("output", outputNode);
        }
    }
}
