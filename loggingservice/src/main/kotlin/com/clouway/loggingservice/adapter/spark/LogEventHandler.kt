package com.clouway.loggingservice.adapter.spark

import com.clouway.loggingservice.core.Log
import com.clouway.loggingservice.core.Logger
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.EventWithAttributes
import org.eclipse.jetty.http.HttpStatus
import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
@Suppress("UNCHECKED_CAST")
class LogEventHandler(private val logger: Logger) : EventHandler {

    override fun handle(eventWithAttributes: EventWithAttributes): Any? {
        return try{
            val eventType = Class.forName(eventWithAttributes.attributes["eventType"] as String)
            val event = eventWithAttributes.event
            val time = eventWithAttributes.attributes["time"] as LocalDateTime
            val log = Log(event, eventType, time)
            logger.storeLog(log)
        }catch (e: Exception){
            HttpStatus.INTERNAL_SERVER_ERROR_500
        }
    }
}