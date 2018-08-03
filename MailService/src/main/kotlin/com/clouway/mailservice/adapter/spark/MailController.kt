package com.clouway.mailservice.adapter.spark

import com.google.appengine.repackaged.com.google.gson.Gson
import com.google.pubsub.v1.PubsubMessage
import org.eclipse.jetty.http.HttpStatus
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

        val properties = Properties()
        val session = Session.getDefaultInstance(properties, null)

        val gson = Gson()
        val message = gson.fromJson(request.body(), PubsubMessage::class.java)
        val email = message.data.toStringUtf8()

        return try{
            val msg = MimeMessage(session)
            msg.setFrom(InternetAddress("anything@sacred-union-210613.appspotmail.com",
                    "Sacred union Admin"))
            msg.addRecipient(Message.RecipientType.TO,
                    InternetAddress(email, "Mr. User"))
            msg.subject = "Push subscriber welcomes you to the spark bank!"
            msg.setText("This is a test email sent by the Push Subscriber")
            Transport.send(msg)
            response.status(HttpStatus.OK_200)
        }catch (e: AddressException){
            e.printStackTrace()
            response.status(HttpStatus.BAD_REQUEST_400)
        }catch (e: MessagingException){
            e.printStackTrace()
            response.status(HttpStatus.BAD_REQUEST_400)
        }catch (e: UnsupportedEncodingException){
            e.printStackTrace()
            response.status(HttpStatus.BAD_REQUEST_400)
        }
    }
}