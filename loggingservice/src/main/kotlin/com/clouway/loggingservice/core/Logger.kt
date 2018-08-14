package com.clouway.loggingservice.core

import java.time.LocalDateTime

/**
 * Stores the event, event type, and time of the event creation
 * into persistence.
 *
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface Logger {
    /**
     * Stores an event with its type and time
     * into persistence.
     *
     * @param log Log to be stored
     * @return The saved log
     */
    fun storeLog(log: Log): Log

    /**
     * Retrieves a list of logs on
     * a certain date and time.
     *
     * @param time Time to retrieve logs from
     * @return List of logs
     */
    fun getLogsFrom(time: LocalDateTime): List<Log>

    /**
     * Retrieves a list of logs
     * between two dates and times.
     *
     * @param from Time to start search from
     * @param to Time to stop search at
     * @return List of logs
     */
    fun getLogsBetween(from: LocalDateTime, to: LocalDateTime): List<Log>
}