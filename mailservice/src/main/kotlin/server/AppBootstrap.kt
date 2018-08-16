package server

import com.clouway.mailservice.adapter.spark.MailEventHandler
import com.clouway.mailservice.core.SendGridMailer
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.UserLoggedOutEvent
import com.clouway.pubsub.core.event.UserRegisteredEvent
import com.clouway.pubsub.factory.EventBusFactory
import com.google.cloud.ServiceOptions
import spark.Spark.post
import spark.servlet.SparkApplication

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppBootstrap : SparkApplication {
    override fun init() {

        val projectId = ServiceOptions.getDefaultProjectId()
        val mailServiceUrl = "https://mail-dot-$projectId.appspot.com"
        val pushUrl = "/_ah/push-handlers/pubsub/mail"

        val eventBus = EventBusFactory.createAsyncPubsubEventBus()
        val mailer = SendGridMailer()

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
                "$mailServiceUrl$pushUrl/message")

        post("$pushUrl/message", eventBus.register(handlerMap))

    }
}