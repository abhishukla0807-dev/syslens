/**
 * FileExporter.java
 *
 * Description:
 *   FileExporter is responsible for saving system snapshots to disk
 *   in different formats, e.g., plain text, JSON, HTML. It ensures reports
 *   are stored in a dedicated directory with timestamped filenames.
 *
 * Purpose:
 *   - Provide persistent storage of monitoring results.
 *   - Allow contributors to extend SysLens with additional export formats.
 *   - Demonstrate integration with Java I/O and formatter modules.
 *
 * Notes for Contributors:
 *   - Reports are saved in the "syslens-reports" directory by default.
 *   - Filenames include a timestamp to avoid overwriting.
 *   - Extend by adding new export methods, e.g., CSV, XML.
 *   - Use the write() helper for consistent file output handling.
 */

package com.aurexiris.syslens.export;

import com.aurexiris.syslens.core.SystemSnapshot;
import com.aurexiris.syslens.output.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileExporter {

    private static final String DEFAULT_DIR = "syslens-reports";

    /**
     * Export the given system snapshot to a file in the requested format.
     * Supported formats: text (default), JSON, HTML.
     *
     * @param snapshot the system snapshot to export
     * @param format   the desired output format
     */
    public void export(SystemSnapshot snapshot, String format) {
        Path dir = Paths.get(DEFAULT_DIR);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Could not create reports directory: " + e.getMessage());
            return;
        }

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        switch (format.toLowerCase()) {
            case "json" -> exportJson(snapshot, dir, timestamp);
            case "html" -> exportHtml(snapshot, dir, timestamp);
            default     -> exportText(snapshot, dir, timestamp);
        }
    }

    // Export snapshot as plain text
    private void exportText(SystemSnapshot snapshot, Path dir, String ts) {
        String fileName = "syslens_" + ts + ".txt";
        String content  = new PlainTextFormatter().format(snapshot);
        write(dir.resolve(fileName), content);
        System.out.println("Report saved → " + dir + "/" + fileName);
    }

    // Export snapshot as JSON
    private void exportJson(SystemSnapshot snapshot, Path dir, String ts) {
        String fileName = "syslens_" + ts + ".json";
        String content  = new JsonFormatter().format(snapshot);
        write(dir.resolve(fileName), content);
        System.out.println("JSON saved   → " + dir + "/" + fileName);
    }

    // Export snapshot as HTML
    private void exportHtml(SystemSnapshot snapshot, Path dir, String ts) {
        String fileName = "syslens_" + ts + ".html";
        String content  = new HtmlFormatter().format(snapshot);
        write(dir.resolve(fileName), content);
        System.out.println("HTML saved   → " + dir + "/" + fileName);
    }

    // Helper method to write content to file
    private void write(Path path, String content) {
        try (FileWriter fw = new FileWriter(path.toFile())) {
            fw.write(content);
        }
        catch (IOException e) {
            System.err.println("Failed to write file: " + e.getMessage());
        }
    }
}
