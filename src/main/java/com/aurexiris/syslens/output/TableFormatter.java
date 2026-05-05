/**
 * TableFormatter.java
 *
 * Author: Aman
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   TableFormatter is an OutputFormatter implementation that converts
 *   a SystemSnapshot into a structured plain text table-style report.
 *   It uses fixed-width borders and centered headings to organize
 *   results into visually distinct sections.
 *
 * Purpose:
 *   - Provide a clean, console-friendly tabular format for SysLens reports.
 *   - Ensure readability by aligning section headers and using separators.
 *   - Allow contributors to extend formatting with additional table styles
 *     or dynamic column layouts.
 *
 * Notes for Contributors:
 *   - Uses snapshot.getTextResults() to retrieve module outputs.
 *   - WIDTH constant defines the table width (default: 60 characters).
 *   - Extend by adjusting border characters or adding column alignment logic.
 *   - Keep formatting consistent across modules for professional output.
 */

package com.aurexiris.syslens.output;

import com.aurexiris.syslens.core.SystemSnapshot;

import java.util.Map;

public class TableFormatter implements OutputFormatter {

    private static final int WIDTH = 60;

    /**
     * Format the given SystemSnapshot into a table-style plain text report.
     * Includes centered headers and bordered sections for each collector.
     * @param snapshot the system snapshot containing results
     * @return formatted string representing the report
     */
    @Override
    public String format(SystemSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(border("═")).append("\n");
        sb.append(center("SYSLENS v1.0.0 — SYSTEM REPORT")).append("\n");
        sb.append(border("═")).append("\n\n");

        // Sections for each collector
        for (Map.Entry<String, String> entry : snapshot.getTextResults().entrySet()) {
            sb.append(border("─")).append("\n");
            sb.append(center("▶  " + entry.getKey())).append("\n");
            sb.append(border("─")).append("\n");
            sb.append(entry.getValue()).append("\n");
        }

        // Footer
        sb.append(border("═")).append("\n");
        sb.append(center("END OF REPORT")).append("\n");
        sb.append(border("═")).append("\n");

        return sb.toString();
    }

    // Helper method to generate a border line
    private String border(String ch) {
        return ch.repeat(WIDTH);
    }

    // Helper method to center text within WIDTH
    private String center(String text) {
        int padding = (WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }
}
