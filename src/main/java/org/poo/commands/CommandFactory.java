package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.poo.fileio.CommandInput;

public final class CommandFactory {
    private final ObjectMapper mapper;

    public CommandFactory(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Creates a command based on the command input
     * @param commandInput the command input
     * @return the specific command
     */
    public Command createCommand(final CommandInput commandInput) {
        return switch (commandInput.getCommand()) {
            case "printUsers" -> new PrintUsersCommand(commandInput, mapper);
            case "addAccount" -> new AddAccountCommand(commandInput, mapper);
            case "createCard" -> new CreateCardCommand(commandInput, mapper);
            case "addFunds" -> new AddFundsCommand(commandInput, mapper);
            case "deleteAccount" -> new DeleteAccountCommand(commandInput, mapper);
            case "createOneTimeCard" -> new CreateOneTimeCardCommand(commandInput, mapper);
            case "deleteCard" -> new DeleteCardCommand(commandInput, mapper);
            case "setMinimumBalance" -> new SetMinimumBalanceCommand(commandInput, mapper);
            case "payOnline" -> new PayOnlineCommand(commandInput, mapper);
            case "sendMoney" -> new SendMoneyCommand(commandInput, mapper);
            case "setAlias" -> new SetAliasCommand(commandInput, mapper);
            case "printTransactions" -> new PrintTransactionsCommand(commandInput, mapper);
            case "checkCardStatus" -> new CheckCardStatusCommand(commandInput, mapper);
            case "splitPayment" -> new SplitPaymentCommand(commandInput, mapper);
            case "report" -> new ReportCommand(commandInput, mapper);
            case "spendingsReport" -> new SpendingsReportCommand(commandInput, mapper);
            case "changeInterestRate" -> new ChangeInterestRateCommand(commandInput, mapper);
            case "addInterest" -> new AddInterestCommand(commandInput, mapper);
            case "withdrawSavings" -> new WithdrawSavingsCommand(commandInput, mapper);
            default -> null;
        };
    }
}
