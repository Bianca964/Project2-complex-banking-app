package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;

public final class AddFundsCommand extends Command {

    public AddFundsCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        Account accountToAddFunds = bank.getAccountWithIBAN(commandInput.getAccount());
        if (accountToAddFunds != null) {
            accountToAddFunds.deposit(commandInput.getAmount());
        }
    }
}
