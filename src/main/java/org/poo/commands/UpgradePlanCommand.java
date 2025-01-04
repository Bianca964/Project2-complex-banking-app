package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.bank.User;
import org.poo.fileio.CommandInput;

public class UpgradePlanCommand extends Command {

    public UpgradePlanCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        User user = bank.getUserWithAccount(commandInput.getAccount());
        if (user == null) {
            return;
        }

        Account account = user.getAccount(commandInput.getAccount());
        if (account == null) {
            addCommandAndTimestamp(objectNode);
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("error", "Account not found");
            outputNode.put("timestamp", commandInput.getTimestamp());
            objectNode.set("output", outputNode);
            return;
        }

        try {
            user.upgradePlan(account, bank, commandInput.getTimestamp(), commandInput.getNewPlanType());
        } catch (Exception e) {
            addCommandAndTimestamp(objectNode);
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("error", e.getMessage());
            objectNode.set("output", outputNode);
        }

    }
}
