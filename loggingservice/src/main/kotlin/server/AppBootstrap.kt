package server

import com.clouway.loggingservice.adapter.gae.datastore.DatastoreLogger
import com.clouway.loggingservice.adapter.spark.*
import com.clouway.pubsub.core.event.*
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
        val logServiceUrl = "https://log-dot-$projectId.appspot.com"
        val pushUrl = "/_ah/push-handlers/pubsub/message"

        val logger = DatastoreLogger()

        val handlerMap = mapOf<Class<*>, EventHandler>(
                UserRegisteredEvent::class.java to LogEventHandler(logger),
                UserLoginEvent::class.java to LogEventHandler(logger),
                UserTransactionEvent::class.java to LogEventHandler(logger),
                UserLoggedOutEvent::class.java to LogEventHandler(logger)
        )

        val eventBus = EventBusFactory.createAsyncPubsubEventBus()

        eventBus.subscribe("user-change",
                "user-change-logging-service",
                "$logServiceUrl$pushUrl")

        post(pushUrl, eventBus.register(handlerMap))
    }
}