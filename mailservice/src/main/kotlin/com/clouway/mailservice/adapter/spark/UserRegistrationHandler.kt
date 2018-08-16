package com.clouway.mailservice.adapter.spark

import com.clouway.mailservice.core.Mailer
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.UserRegisteredEvent
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class UserRegistrationHandler(private val mailer: Mailer) : EventHandler{
    override fun handle(req: Request, res: Response): Any? {
        return try {
            val event = req.attribute<Event>("event")
            mailer.mail((event as UserRegisteredEvent).email,
                    "Welcome to the spark bank",
                    "This was sent via a push pubsub")
        } catch (e: Exception) {
            e.printStackTrace()
            HttpStatus.INTERNAL_SERVER_ERROR_500
        }
    }
}