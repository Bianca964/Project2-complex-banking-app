package org.poo.commands.accountcommands.savingsaccountcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.users.User;
import org.poo.fileio.CommandInput;

public final class WithdrawSavingsCommand extends Command {

    public WithdrawSavingsCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        User user = bank.getUserWithAccount(commandInput.getAccount());

        if (user != null) {
            bank.withdrawSavings(commandInput);
        }
    }
}
