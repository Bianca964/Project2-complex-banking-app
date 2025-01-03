package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.accounts.ClassicAccount;
import org.poo.accounts.SavingsAccount;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;

public final class AddAccountCommand extends Command {

    public AddAccountCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        String accountType = commandInput.getAccountType();
        Account account = accountType.equals("classic")
                ? new ClassicAccount(commandInput.getCurrency(), commandInput.getAccountType(),
                commandInput.getTimestamp())
                : new SavingsAccount(commandInput.getCurrency(), commandInput.getAccountType(),
                commandInput.getTimestamp(), commandInput.getInterestRate());
        bank.addAccountToUser(commandInput.getEmail(), account, accountType);
    }
}
