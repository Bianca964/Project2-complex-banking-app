package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.poo.commands.accountcommands.AddAccountCommand;
import org.poo.commands.accountcommands.AddFundsCommand;
import org.poo.commands.accountcommands.DeleteAccountCommand;
import org.poo.commands.accountcommands.SetMinimumBalanceCommand;
import org.poo.commands.accountcommands.SetAliasCommand;
import org.poo.commands.accountcommands.businessaccountcommands.AddNewBussAssociateCommand;
import org.poo.commands.accountcommands.businessaccountcommands.ChangeDepositLimitCommand;
import org.poo.commands.accountcommands.businessaccountcommands.ChangeSpendingLimitCommand;
import org.poo.commands.accountcommands.savingsaccountcommands.AddInterestCommand;
import org.poo.commands.accountcommands.savingsaccountcommands.ChangeInterestRateCommand;
import org.poo.commands.accountcommands.savingsaccountcommands.WithdrawSavingsCommand;
import org.poo.commands.cardcommands.CheckCardStatusCommand;
import org.poo.commands.cardcommands.CreateCardCommand;
import org.poo.commands.cardcommands.CreateOneTimeCardCommand;
import org.poo.commands.cardcommands.DeleteCardCommand;
import org.poo.commands.reportscommands.BusinessReportCommand;
import org.poo.commands.reportscommands.ReportCommand;
import org.poo.commands.reportscommands.SpendingsReportCommand;
import org.poo.commands.splitpaymentcommands.AcceptSplitPaymentCommand;
import org.poo.commands.splitpaymentcommands.RejectSplitPaymentCommand;
import org.poo.commands.splitpaymentcommands.SplitPaymentCommand;
import org.poo.commands.transactionscommands.CashWithdrawalCommand;
import org.poo.commands.transactionscommands.PayOnlineCommand;
import org.poo.commands.transactionscommands.PrintTransactionsCommand;
import org.poo.commands.transactionscommands.SendMoneyCommand;
import org.poo.commands.usercommands.PrintUsersCommand;
import org.poo.commands.usercommands.UpgradePlanCommand;
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
            case "upgradePlan" -> new UpgradePlanCommand(commandInput, mapper);
            case "cashWithdrawal" -> new CashWithdrawalCommand(commandInput, mapper);
            case "acceptSplitPayment" -> new AcceptSplitPaymentCommand(commandInput, mapper);
            case "addNewBusinessAssociate" -> new AddNewBussAssociateCommand(commandInput, mapper);
            case "changeSpendingLimit" -> new ChangeSpendingLimitCommand(commandInput, mapper);
            case "businessReport" -> new BusinessReportCommand(commandInput, mapper);
            case "changeDepositLimit" -> new ChangeDepositLimitCommand(commandInput, mapper);
            case "rejectSplitPayment" -> new RejectSplitPaymentCommand(commandInput, mapper);
            default -> null;
        };
    }
}
