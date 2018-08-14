package com.clouway.loggingservice.adapter.spark

import com.clouway.loggingservice.core.Log
import com.clouway.loggingservice.core.Logger
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
@Suppress("UNCHECKED_CAST")
class LogEventHandler(private val logger: Logger) : EventHandler {

    override fun handle(req: Request, res: Response): Any? {
        return try{
            val eventType = Class.forName(req.attribute("eventType"))
            val event = req.attribute<Event>("event")
            val time = req.attribute<LocalDateTime>("time")
            val log = Log(event, eventType, time)
            logger.storeLog(log)
        }catch (e: Exception){
            HttpStatus.INTERNAL_SERVER_ERROR_500
        }
    }
}