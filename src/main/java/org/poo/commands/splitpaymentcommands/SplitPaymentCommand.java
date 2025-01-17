package org.poo.commands.splitpaymentcommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.users.User;
import org.poo.fileio.CommandInput;
import org.poo.transactions.SplitPaymentEqual;
import org.poo.transactions.SplitPayment;
import org.poo.transactions.SplitPaymentCustom;

import java.util.ArrayList;

public final class SplitPaymentCommand extends Command {

    public SplitPaymentCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {

        ArrayList<User> users = new ArrayList<>();
        for (String accountIBAN : commandInput.getAccounts()) {
            User user = bank.getUserWithAccount(accountIBAN);
            if (user != null) {
                users.add(user);
            }
        }

        SplitPayment splitPayment;
        if (commandInput.getSplitPaymentType().equals("equal")) {
            splitPayment = new SplitPaymentEqual(bank, commandInput);

            // tb sa adaug splitPayment la Useri
            for (User user : users) {
                user.addSplitPayment(splitPayment);
            }

        } else if (commandInput.getSplitPaymentType().equals("custom")) {
            splitPayment = new SplitPaymentCustom(bank, commandInput);

            // tb sa adaug splitPayment la USER
            for (User user : users) {
                user.addSplitPayment(splitPayment);
            }
        }
    }
}
