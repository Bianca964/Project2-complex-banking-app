package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.bank.Bank;
import org.poo.fileio.CommandInput;

public class BusinessReportCommand extends Command {

    public BusinessReportCommand(final CommandInput commandInput, final ObjectMapper mapper) {
        super(commandInput, mapper);
    }

    @Override
    public void execute(final Bank bank, final ObjectNode objectNode) {

        addCommandAndTimestamp(objectNode);
        try {
            objectNode.set("output", bank.businessReport(commandInput, mapper));
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
