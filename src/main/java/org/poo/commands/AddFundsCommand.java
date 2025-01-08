package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

public final class AddFundsCommand extends Command {

    public AddFundsCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        Account accountToAddFunds = bank.getAccountWithIBAN(commandInput.getAccount());
        if (accountToAddFunds != null) {
            User user = bank.getUserWithEmail(commandInput.getEmail());
            try {
                accountToAddFunds.addFunds(commandInput.getAmount(), user);
            } catch (Exception e) {
//                addCommandAndTimestamp(objectNode);
//                ObjectNode outputNode = mapper.createObjectNode();
//                outputNode.put("error", e.getMessage());
//                outputNode.put("timestamp", commandInput.getTimestamp());
//                objectNode.set("output", outputNode);
                return;
            }

            //accountToAddFunds.deposit(commandInput.getAmount(), user);
        }
    }
}
