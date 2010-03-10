/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daftsolutions.lib.log;

import org.apache.log4j.Logger;

/**
 * Implement a basic logging mechanism
 * @author colin
 */
public class EventLogger {
    private static Logger logger = Logger.getLogger(EventLogger.class);

    public enum StatusValues { UNKNOWN, SUCCESS, FAILURE, WARNING }

    public EventLogger() {

    }

    /**
     * Just log some text, no structure
     * Best not to mix with structured logs, as analysis of log file then not so easy
     * @param text
     */
    public void log(String text) {
        logger.info(text);
    }

    /**
     * Log a structured message.
     * @param status
     * @param catalog
     * @param userName
     * @param id
     * @param message
     */
    public void log(EventLogger.StatusValues status, String catalog, int id, String userName, String message, String comment) {
        logger.info(String.format("%s,%s,%d,%s,%s,%s", status.toString(), catalog, id, userName, message, comment));
    }
    
}
