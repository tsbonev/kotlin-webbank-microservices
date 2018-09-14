package com.clouway.transactionlistener.adapter.spark

import com.clouway.pubsub.core.event.CounterUpdatedEvent
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.transactionlistener.core.CounterUpdater
import com.google.appengine.repackaged.com.google.gson.Gson
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class CounterUpdatedEventHandler(private val saver: CounterUpdater) : EventHandler {
    override fun handle(req: Request, res: Response): Any? {
        return try {
            val gson = Gson()
            val eventJson = gson.toJson(req.attribute<Event>("event"))
            val updateEvent = gson.fromJson(eventJson, CounterUpdatedEvent::class.java)
            res.status(saver.update(updateEvent.counterId))
        } catch (e: Exception) {
            e.printStackTrace()
            res.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }
    }
}