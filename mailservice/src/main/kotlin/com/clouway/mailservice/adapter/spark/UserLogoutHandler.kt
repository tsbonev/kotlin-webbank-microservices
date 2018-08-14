package com.clouway.mailservice.adapter.spark

import com.clouway.mailservice.core.Mailer
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.UserLoggedOutEvent
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class UserLogoutHandler(private val mailer: Mailer) : EventHandler{
    override fun handle(req: Request, res: Response): Any? {
        return try {
            val event = req.attribute<Event>("event")
            mailer.mail((event as UserLoggedOutEvent).email,
                    "You have logged out of the spark bank",
                    "This was sent via a push pubsub")
        } catch (e: Exception) {
            HttpStatus.NO_CONTENT_204
        }
    }
}