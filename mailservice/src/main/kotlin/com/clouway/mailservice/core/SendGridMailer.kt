package com.clouway.mailservice.core

import com.sendgrid.*
import org.eclipse.jetty.http.HttpStatus
import java.io.IOException

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class SendGridMailer(private val apikey: String) : Mailer {
    override fun mail(receiver: String, title: String, content: String): Int {
        val from = Email("admin@sacred-union.com")
        val to = Email(receiver)
        val mailContent = Content("text/plain", content)
        val mail = Mail(from, title, to, mailContent)

        val sg = SendGrid(apikey)
        val request = Request()
        return try {
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            sg.api(request)
            HttpStatus.OK_200
        } catch (e: IOException) {
            e.printStackTrace()
            HttpStatus.NO_CONTENT_204
        }
    }
}