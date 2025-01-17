package org.poo.commands.accountcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;

public final class SetMinimumBalanceCommand extends Command {

    public SetMinimumBalanceCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        Account account = bank.getAccountWithIBAN(commandInput.getAccount());
        if (account != null) {
            account.setMinimumBalance(commandInput.getAmount());
        }
    }
}
