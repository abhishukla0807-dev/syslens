
/**
 * InfoCollector
 * Central manager that coordinates all system information modules
 * and aggregates their data into a unified SystemSnapshot.
 */

package com.aurexiris.syslens.core;


//Used to collect system information and return it in both plain text and JSON formats.
// Each implementation of this interface will represent a specific module of system information (e.g., CPU Info, Memory Info, etc.).
public interface InfoCollector {
    String getName();       // module name e.g. "CPU Info"
    String collect();       // returns plain text output
    String toJson();        // returns JSON output
}