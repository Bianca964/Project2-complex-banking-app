package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.accounts.Account;
import org.poo.accounts.BusinessAccount;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;
import org.poo.users.User;

public class AddNewBusinessAssociateCommand extends Command {

    public AddNewBusinessAssociateCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        Account account = bank.getAccountWithIBAN(commandInput.getAccount());
        User associate = bank.getUserWithEmail(commandInput.getEmail());

        if (account != null && account.isBusinessAccount() && associate != null) {
            ((BusinessAccount) account).addAssociate(associate, commandInput.getRole());
            System.out.println("Associate " + associate.getEmail() + " added to business account " + account.getIban() + " with role " + commandInput.getRole() + " at timestamp " + commandInput.getTimestamp());
        }
    }
}
