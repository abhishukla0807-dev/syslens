/**
 * JsonFormatter.java
 *
 * Author: Abhishek Shukla
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   JsonFormatter is an OutputFormatter implementation that converts
 *   a SystemSnapshot into a structured JSON report. It organizes results
 *   into key-value pairs with metadata such as tool name, version, and timestamp.
 *
 * Purpose:
 *   - Provide machine-readable output for SysLens reports.
 *   - Support integration with external systems, dashboards, or APIs.
 *   - Allow contributors to extend JSON schema with additional metadata.
 *
 * Notes for Contributors:
 *   - Uses snapshot.getJsonResults() for module-specific JSON data.
 *   - Timestamp is generated at runtime in "dd-MM-yyyy HH:mm:ss" format.
 *   - Extend by adding new metadata fields or nested structures.
 *   - Ensure JSON formatting remains valid and consistent.
 */

package com.aurexiris.syslens.output;

import com.aurexiris.syslens.core.SystemSnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class JsonFormatter implements OutputFormatter {

    /**
     * Format the given SystemSnapshot into a structured JSON report.
     * @param snapshot the system snapshot containing results
     * @return JSON string representing the report
     */
    @Override
    public String format(SystemSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        sb.append("{\n");
        sb.append("  \"tool\": \"SysLens\",\n");
        sb.append("  \"version\": \"1.0.0\",\n");
        sb.append(String.format("  \"timestamp\": \"%s\",%n", timestamp));
        sb.append("  \"data\": {\n");

        Map<String, String> jsonResults = snapshot.getJsonResults();
        int i = 0;
        for (Map.Entry<String, String> entry : jsonResults.entrySet()) {
            sb.append(String.format("    \"%s\": %s", entry.getKey(), entry.getValue()));
            if (++i < jsonResults.size()) sb.append(",");
            sb.append("\n");
        }

        sb.append("  }\n}");
        return sb.toString();
    }
}
