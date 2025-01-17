package org.poo.commands.reportscommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;
import org.poo.reports.BusinessReport;
import org.poo.reports.ReportGenerator;

public class BusinessReportCommand extends Command {

    public BusinessReportCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {

        addCommandAndTimestamp(objectNode);
        try {
            ReportGenerator reportGenerator = new BusinessReport(bank);
            objectNode.set("output", bank.generateReport(commandInput, mapper, reportGenerator));
        } catch (Exception e) {
            ObjectNode outputNode = mapper.createObjectNode();
            if (e.getMessage().equals("This kind of report is not supported for a saving "
                    + "account")) {
                outputNode.put("error", e.getMessage());
                objectNode.set("output", outputNode);
            } else {
                outputNode.put("description", e.getMessage());
                outputNode.put("timestamp", commandInput.getTimestamp());
                objectNode.set("output", outputNode);
            }
        }

    }
}
