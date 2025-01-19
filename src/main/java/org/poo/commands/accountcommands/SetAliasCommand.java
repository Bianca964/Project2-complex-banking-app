package org.poo.commands.accountcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

public final class SetAliasCommand extends Command {

    public SetAliasCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        User user = bank.getUserWithEmail(commandInput.getEmail());
        Account account = bank.getAccountWithIBAN(commandInput.getAccount());
        if (account != null) {
            account.setAlias(commandInput.getAlias(), user);
        }
    }
}
