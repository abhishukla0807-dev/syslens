/**
 * ClipboardExporter.java
 *
 * Description:
 *   ClipboardExporter is responsible for exporting a system snapshot
 *   into the system clipboard. It uses the PlainTextFormatter to
 *   convert snapshot data into a human-readable format before copying.
 *
 * Purpose:
 *   - Provide quick access to monitoring results without saving files.
 *   - Allow contributors to extend SysLens with alternative clipboard
 *     formats, e.g., JSON, HTML.
 *   - Demonstrate integration with Java AWT clipboard APIs.
 *
 * Notes for Contributors:
 *   - Currently uses PlainTextFormatter for output.
 *   - Consider adding support for other formatters if needed.
 *   - Clipboard operations may fail in headless environments
 *     (e.g., servers without GUI).
 */

package com.aurexiris.syslens.export;

import com.aurexiris.syslens.core.SystemSnapshot;
import com.aurexiris.syslens.output.PlainTextFormatter;

import java.awt.*;
import java.awt.datatransfer.*;

public class ClipboardExporter {

    /**
     * Export the given system snapshot to the system clipboard.
     * @param snapshot the system snapshot to export
     */
    public void export(SystemSnapshot snapshot) {
        String content = new PlainTextFormatter().format(snapshot);

        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            StringSelection selection = new StringSelection(content);
            clipboard.setContents(selection, selection);
            System.out.println("Report copied to clipboard!");
        }
        catch (Exception e) {
            System.err.println("Clipboard export failed: " + e.getMessage());
        }
    }
}
