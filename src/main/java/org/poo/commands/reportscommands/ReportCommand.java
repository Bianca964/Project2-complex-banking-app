package org.poo.commands.reportscommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.commands.Command;
import org.poo.fileio.CommandInput;
import org.poo.reports.ClassicReport;
import org.poo.reports.ReportGenerator;

public final class ReportCommand extends Command {

    public ReportCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {
        addCommandAndTimestamp(objectNode);
        try {
            ReportGenerator reportGenerator = new ClassicReport(bank);
            objectNode.set("output", bank.generateReport(commandInput, mapper, reportGenerator));
        } catch (Exception e) {
            ObjectNode outputNode = mapper.createObjectNode();
            outputNode.put("description", e.getMessage());
            outputNode.put("timestamp", commandInput.getTimestamp());
            objectNode.set("output", outputNode);
        }
    }
}
