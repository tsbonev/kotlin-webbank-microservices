package com.clouway.mailservice.adapter.spark

import spark.Request
import spark.Response
import spark.Route
import java.io.UnsupportedEncodingException
import java.util.*
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MailController : Route {
    override fun handle(request: Request, response: Response): Any {

        println("-------- EMAIL REQUEST RECEIVED--------")

        val properties = Properties()
        val session = Session.getDefaultInstance(properties, null)
        val email = request.queryParams("email")

        return try{
            val msg = MimeMessage(session)
            msg.setFrom(InternetAddress("anything@sacred-union-210613.appspotmail.com",
                    "Sacred union Admin"))
            msg.addRecipient(Message.RecipientType.TO,
                    InternetAddress(email, "Mr. User"))
            msg.subject = "Your request has been received"
            msg.setText("This is a test")
            Transport.send(msg)
        }catch (e: AddressException){
            e.printStackTrace()
        }catch (e: MessagingException){
            e.printStackTrace()
        }catch (e: UnsupportedEncodingException){
            e.printStackTrace()
        }
    }
}