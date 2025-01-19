package org.poo.reports.businessreport;

import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;

public final class BusinessReportFactory {

    /**
     * Private constructor to not allow instantiation
     */
    private BusinessReportFactory() {
    }

    /**
     * Creates the type of business report required
     * @param bank the bank for constructing the reports
     * @param commandInput the command input containing the type required
     * @return an instance of the subtype of business report required
     * @throws Exception if any errors occurs
     */
    public static BusinessReport createBusinessReport(final CommandInput commandInput,
                                                      final Bank bank) throws Exception {
        String type = commandInput.getType();
        return switch (type) {
            case "commerciant" -> new CommerciantReport(bank);
            case "transaction" -> new TransactionReport(bank);
            default -> throw new Exception("Unsupported report type");
        };
    }
}
