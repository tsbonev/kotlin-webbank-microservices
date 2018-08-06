package com.clouway.mailservice.adapter.spark

import com.clouway.mailservice.core.JavaMailer
import com.clouway.mailservice.core.Mailer
import spark.Request
import spark.Response


/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MailController(private val mailer: Mailer = JavaMailer()) : Controller {
    override fun handle(request: Request, response: Response): Any? {
        val email = request.attribute<String>("data")
        return mailer.mail(email, "Welcome to the spark bank", "This was sent via a push pubsub")
    }
}