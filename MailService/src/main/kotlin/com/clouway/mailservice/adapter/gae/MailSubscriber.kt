package com.clouway.mailservice.adapter.gae

import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import java.io.UnsupportedEncodingException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
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
class MailSubscriber {

    private fun sendMail(email: String){
        val properties = Properties()
        val session = Session.getDefaultInstance(properties, null)

        return try{
            val msg = MimeMessage(session)
            msg.setFrom(InternetAddress("anything@sacred-union-210613.appspotmail.com",
                    "Sacred union Admin"))
            msg.addRecipient(Message.RecipientType.TO,
                    InternetAddress(email, "Mr. User"))
            msg.subject = "Pull subscriber welcomes you to the spark bank!"
            msg.setText("This is a test email sent from the Pull Subscriber")
            Transport.send(msg)
        }catch (e: AddressException){
            e.printStackTrace()
        }catch (e: MessagingException){
            e.printStackTrace()
        }catch (e: UnsupportedEncodingException){
            e.printStackTrace()
        }
    }

    val projectId = ServiceOptions.getDefaultProjectId()

    private val subscriptionId = "mail-registered"

    private val messages = LinkedBlockingQueue<PubsubMessage>()

    private val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId)!!
    private val receiver = MessageReceiver { message, consumer ->
        messages.offer(message)
        consumer.ack()
    }

    private val subscriber: Subscriber by lazy {Subscriber.newBuilder(subscriptionName, receiver).build()}

    init {
        try{
            subscriber.startAsync().awaitRunning()

            while(true){
                val message = messages.take()
                sendMail(message.data.toStringUtf8())
            }

        }finally {
                subscriber.stopAsync()
        }
    }
}
