/**
 * PlainTextFormatter.java
 *
 * Author: Abhishek Shukla
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   PlainTextFormatter is an OutputFormatter implementation that converts
 *   a SystemSnapshot into a simple, human-readable plain text report.
 *   It uses ASCII art banners to visually separate sections of the report.
 *
 * Purpose:
 *   - Provide a lightweight, console-friendly format for SysLens reports.
 *   - Ensure readability without requiring external tools or styling.
 *   - Allow contributors to extend formatting with additional banners or
 *     structured separators.
 *
 * Notes for Contributors:
 *   - Uses snapshot.getAllText() to aggregate results from all collectors.
 *   - Extend by adding new banners or adjusting ASCII art for clarity.
 *   - Keep formatting consistent across modules for professional output.
 */

package com.aurexiris.syslens.output;

import com.aurexiris.syslens.core.SystemSnapshot;

public class PlainTextFormatter implements OutputFormatter {

    /**
     * Format the given SystemSnapshot into a plain text report.
     * Includes ASCII banners at the start and end of the report.
     * @param snapshot the system snapshot containing results
     * @return plain text string representing the report
     */
    @Override
    public String format(SystemSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();

        // Header banner
        sb.append("""
                
                ╔══════════════════════════════╗
                ║      SYSLENS  v1.0.0         ║
                ║   System Information Tool    ║
                ╚══════════════════════════════╝
                """);

        // Append all collected text results
        sb.append(snapshot.getAllText());

        // Footer banner
        sb.append("""
                ╔══════════════════════════════╗
                ║     END OF SYSTEM REPORT     ║
                ╚══════════════════════════════╝
                """);

        return sb.toString();
    }
}
