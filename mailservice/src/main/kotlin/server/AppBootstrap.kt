package server

import com.clouway.mailservice.adapter.spark.MailEventHandler
import com.clouway.mailservice.core.SendGridMailer
import com.clouway.pubsub.core.event.UserLoggedOutEvent
import com.clouway.pubsub.core.event.UserRegisteredEvent
import com.clouway.pubsub.factory.PubsubFactory
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


        val apiKeyFile = GcsFilename("sacred-union-210613.appspot.com",
                "sendgrid.env")

        val gscReadChannel = GcsServiceFactory
                .createGcsService()
                .openPrefetchingReadChannel(apiKeyFile, 0, DEFAULT_BUFFER_SIZE)

        val sendgridApiKey = CharStreams.toString(InputStreamReader(
                Channels.newInputStream(gscReadChannel), Charsets.UTF_8))

        val mailer = SendGridMailer(sendgridApiKey.trim())

        val subscription = PubsubFactory.createPubsubSubscription("user-change",
                "user-change-mail-service",
                "$mailServiceUrl$pushUrl")

        subscription.registerEventHandler(UserLoggedOutEvent::class.java,
                MailEventHandler(
                        "You have logged out of the spark bank",
                        "This was sent via a push pubsub",
                        mailer
                ))

        subscription.registerEventHandler(UserRegisteredEvent::class.java,
                MailEventHandler(
                        "Welcome to the spark bank",
                        "This was sent via a push pubsub",
                        mailer))

        post(pushUrl) {
            req, res -> subscription.handle(req.raw(), res.raw())
        }

    }
}