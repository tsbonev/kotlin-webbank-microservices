package com.clouway.mailservice.core

import org.eclipse.jetty.http.HttpStatus
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
class JavaMailer : Mailer {
    override fun mail(receiver: String, title: String, content: String): Int {
        return try{

            val properties = Properties()
            val session = Session.getDefaultInstance(properties, null)

            val msg = MimeMessage(session)
            msg.setFrom(InternetAddress("anything@sacred-union-210613.appspotmail.com",
                    "Sacred union Admin"))
            msg.addRecipient(Message.RecipientType.TO,
                    InternetAddress(receiver, "Mr. User"))
            msg.subject = title
            msg.setText(content)
            Transport.send(msg)
            HttpStatus.OK_200
        }catch (e: AddressException){
            e.printStackTrace()
            HttpStatus.NO_CONTENT_204
        }catch (e: MessagingException){
            e.printStackTrace()
            HttpStatus.NO_CONTENT_204
        }catch (e: UnsupportedEncodingException){
            e.printStackTrace()
            HttpStatus.NO_CONTENT_204
        }
    }
}