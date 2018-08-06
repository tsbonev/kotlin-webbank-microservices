package com.clouway.mailservice.adapter.spark

import com.google.appengine.repackaged.com.google.gson.Gson
import com.google.pubsub.v1.PubsubMessage
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
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

data class PubsubMessageWrapper(val attributes: Any, val data: String, val message_id: String)
data class PubsubWrapper(val message: PubsubMessageWrapper, val subscription: String)

class MailController : Route {
    override fun handle(request: Request, response: Response): Any {

        val properties = Properties()
        val session = Session.getDefaultInstance(properties, null)

        return try{

            val gson = Gson()
            val messageStream = request.raw().inputStream
            val stringBuilder = StringBuilder()

            BufferedReader(InputStreamReader(
                    messageStream, Charset.forName(StandardCharsets.UTF_8.name()))).use {

                var next = 0
                fun getNext(): Int{
                    next = it.read()
                    return next
                }

                while(getNext() != -1){
                    stringBuilder.append(next.toChar())
                }
            }

            val pubsub = gson.fromJson(stringBuilder.toString(), PubsubWrapper::class.java)
            val email = String(Base64.getDecoder().decode(pubsub.message.data))

            val msg = MimeMessage(session)
            msg.setFrom(InternetAddress("anything@sacred-union-210613.appspotmail.com",
                    "Sacred union push Admin"))
            msg.addRecipient(Message.RecipientType.TO,
                    InternetAddress(email, "Mr. User $email"))
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