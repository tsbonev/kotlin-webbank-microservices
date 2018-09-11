package server

import com.clouway.loggingservice.adapter.gae.datastore.DatastoreLogger
import com.clouway.loggingservice.adapter.spark.*
import com.clouway.pubsub.core.event.*
import com.clouway.pubsub.factory.PubsubFactory
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

        val subscription = PubsubFactory.createPubsubSubscription("user-change",
                "user-change-logging-service",
                "$logServiceUrl$pushUrl")

        subscription.registerEventHandler(UserRegisteredEvent::class.java, LogEventHandler(logger))
        subscription.registerEventHandler(UserLoginEvent::class.java, LogEventHandler(logger))
        subscription.registerEventHandler(UserTransactionEvent::class.java, LogEventHandler(logger))
        subscription.registerEventHandler(UserLoggedOutEvent::class.java, LogEventHandler(logger))

        post(pushUrl) {
            req, res -> subscription.handle(req.raw(), res.raw())
        }
    }
}