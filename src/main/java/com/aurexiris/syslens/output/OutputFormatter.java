/**
 * OutputFormatter.java
 *
 * Author: Abhishek Shukla
 * Version: 1.0.0
 * Last Updated: 2026-05-05
 *
 * Description:
 *   OutputFormatter is a functional interface that defines the contract
 *   for formatting a SystemSnapshot into a specific output representation.
 *   Implementations include PlainTextFormatter, HtmlFormatter, JsonFormatter,
 *   and potentially others (e.g., MarkdownFormatter, YamlFormatter).
 *
 * Purpose:
 *   - Provide a unified interface for different output formats in SysLens.
 *   - Allow contributors to easily add new formatters without modifying
 *     existing code.
 *   - Support extensibility and clean architecture by decoupling snapshot
 *     collection from output rendering.
 *
 * Notes for Contributors:
 *   - Implementations must override the format() method.
 *   - Ensure consistent handling of SystemSnapshot data across formats.
 *   - Follow naming conventions: <FormatName>Formatter.
 *   - Consider adding metadata (tool name, version, timestamp) in outputs
 *     for traceability.
 */

package com.aurexiris.syslens.output;

import com.aurexiris.syslens.core.SystemSnapshot;

public interface OutputFormatter {
    /**
     * Format the given SystemSnapshot into a specific output representation.
     * @param snapshot the system snapshot containing results
     * @return formatted string representing the snapshot
     */
    String format(SystemSnapshot snapshot);
}
