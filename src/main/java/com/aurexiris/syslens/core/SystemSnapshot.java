


/**
 * SystemSnapshot
 * Immutable data structure that stores a snapshot of collected
 * system information at a given point in time.
 */



package com.aurexiris.syslens.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class SystemSnapshot {

    private final Map<String, String> textResults = new LinkedHashMap<>();
    private final Map<String, String> jsonResults = new LinkedHashMap<>();


    /*
    This method adds the collected information from a module to the snapshot.
    It takes the module name, the text output, and the JSON output as parameters and stores them
     */
    public void add(String moduleName, String text, String json) {
        textResults.put(moduleName, text);
        jsonResults.put(moduleName, json);
    }


    /*
    These methods provide access to the collected results. getTextResults() and getJsonResults() return the maps containing the text and JSON outputs, respectively.
    getAllText() and getAllJson() concatenate all the collected information into a single string.
    getAllText() formats the text outputs with newlines for readability, while getAllJson() constructs a JSON object string that includes all the collected JSON outputs, properly formatted with indentation and commas between entries.
     */

    public Map<String, String> getTextResults() { return textResults; }
    public Map<String, String> getJsonResults()  { return jsonResults; }

    public String getAllText() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : textResults.entrySet()) {
            sb.append("\n").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }


    /*
     This method constructs a JSON string that represents all the collected JSON outputs from the modules.
     It iterates through the jsonResults map, appending each module's name and its corresponding JSON output to a StringBuilder.
     The resulting string is formatted as a JSON object, with proper indentation and commas between entries.
    */
    public String getAllJson() {
        StringBuilder sb = new StringBuilder("{\n");
        int i = 0;

        for (Map.Entry<String, String> e : jsonResults.entrySet()) {// Iterate through each entry in the jsonResults map.

            sb.append("  \"").append(e.getKey()).append("\": ")
                    .append(e.getValue());
            if (++i < jsonResults.size()) sb.append(",");// Add a comma after each entry except the last one.
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}