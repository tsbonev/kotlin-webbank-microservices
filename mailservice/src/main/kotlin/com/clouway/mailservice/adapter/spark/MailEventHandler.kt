package com.clouway.mailservice.adapter.spark

import com.clouway.mailservice.core.Mailer
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.google.appengine.repackaged.com.google.gson.Gson
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MailEventHandler(private val title: String,
                       private val content: String,
                       private val mailer: Mailer) : EventHandler {

    data class EmailEvent(val email: String)

    override fun handle(req: Request, res: Response): Any? {
        return try {
            val gson = Gson()
            val eventJson = gson.toJson(req.attribute<Event>("event"))
            val email = gson.fromJson(eventJson, EmailEvent::class.java).email
            mailer.mail(email,
                    title,
                    content)
        } catch (e: Exception) {
            e.printStackTrace()
            HttpStatus.INTERNAL_SERVER_ERROR_500
        }
    }
}