package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.accounts.ClassicAccount;
import org.poo.accounts.SavingsAccount;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

public final class AddAccountCommand extends Command {

    public AddAccountCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        String accountType = commandInput.getAccountType();
        Account account;
        if (accountType.equals("classic")) {
            account = new ClassicAccount(commandInput.getCurrency(), accountType,
                    commandInput.getTimestamp());
        } else if (accountType.equals("savings")) {
            account = new SavingsAccount(commandInput.getCurrency(), accountType,
                    commandInput.getTimestamp(), commandInput.getInterestRate());
        } else { // business account
            //System.out.println("email input = " + commandInput.getEmail());
            User owner = bank.getUserWithEmail(commandInput.getEmail());
            account = new BusinessAccount(commandInput.getCurrency(), accountType,
                    commandInput.getTimestamp(), owner, bank);

            //System.out.println("owner email = " + ((BusinessAccount)account).getOwner().getEmail());
        }

        bank.addAccountToUser(commandInput.getEmail(), account, accountType);
    }
}
