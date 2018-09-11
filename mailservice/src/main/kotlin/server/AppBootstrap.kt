package server

import com.clouway.mailservice.adapter.spark.MailEventHandler
import com.clouway.mailservice.core.SendGridMailer
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.UserLoggedOutEvent
import com.clouway.pubsub.core.event.UserRegisteredEvent
import com.clouway.pubsub.factory.EventBusFactory
import com.google.appengine.repackaged.com.google.common.io.CharStreams
import com.google.appengine.tools.cloudstorage.GcsFilename
import com.google.appengine.tools.cloudstorage.GcsServiceFactory
import com.google.cloud.ServiceOptions
import spark.Spark.post
import spark.servlet.SparkApplication
import java.io.InputStreamReader
import java.nio.channels.Channels

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppBootstrap : SparkApplication {
    override fun init() {

        val projectId = ServiceOptions.getDefaultProjectId()
        val mailServiceUrl = "https://mail-dot-$projectId.appspot.com"
        val pushUrl = "/_ah/push-handlers/pubsub/message"

        val eventBus = EventBusFactory.createAsyncPubsubEventBus()

        val apiKeyFile = GcsFilename("sacred-union-210613.appspot.com",
                "sendgrid.env")

        val gscReadChannel = GcsServiceFactory
                .createGcsService()
                .openPrefetchingReadChannel(apiKeyFile, 0, DEFAULT_BUFFER_SIZE)

        val sendgridApiKey = CharStreams.toString(InputStreamReader(
                Channels.newInputStream(gscReadChannel), Charsets.UTF_8))

        val mailer = SendGridMailer(sendgridApiKey.trim())

        val handlerMap =  mapOf<Class<*>, EventHandler>(
                UserLoggedOutEvent::class.java to MailEventHandler(
                  "You have logged out of the spark bank",
                        "This was sent via a push pubsub",
                        mailer
                ),
                UserRegisteredEvent::class.java to MailEventHandler(
                        "Welcome to the spark bank",
                        "This was sent via a push pubsub",
                        mailer)
        )

        eventBus.subscribe("user-change",
                "user-change-mail-service",
                "$mailServiceUrl$pushUrl")

        post(pushUrl, eventBus.register(handlerMap))

    }
}