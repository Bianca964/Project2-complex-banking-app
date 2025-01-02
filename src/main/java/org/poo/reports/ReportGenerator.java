package org.poo.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;

public interface ReportGenerator {
    /**
     * Generates a report based on the command input
     * @param commandInput the command input
     * @param mapper the object mapper used to create the object node
     * @return the object node containing the report
     * @throws Exception if the report cannot be generated
     */
    ObjectNode generateReport(CommandInput commandInput, ObjectMapper mapper) throws Exception;
}
