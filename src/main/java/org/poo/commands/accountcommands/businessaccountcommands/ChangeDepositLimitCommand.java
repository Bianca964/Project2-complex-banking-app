package org.poo.commands.accountcommands.businessaccountcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

public final class ChangeDepositLimitCommand extends Command {

    public ChangeDepositLimitCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        double newDepositLimit = commandInput.getAmount();
        User userTryingToChangeIt = bank.getUserWithEmail(commandInput.getEmail());
        Account account = bank.getAccountWithIBAN(commandInput.getAccount());

        if (account != null && account.isBusinessAccount() && userTryingToChangeIt != null) {
            try {
                ((BusinessAccount) account).setDepositLimit(newDepositLimit, userTryingToChangeIt);
            } catch (Exception e) {
                addCommandAndTimestamp(objectNode);

                ObjectNode outputNode = mapper.createObjectNode();
                outputNode.put("description", e.getMessage());
                outputNode.put("timestamp", commandInput.getTimestamp());
                objectNode.set("output", outputNode);
            }
        }
    }
}
