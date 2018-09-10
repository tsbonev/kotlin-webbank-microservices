package server

import com.clouway.pubsub.core.EventBus
import com.clouway.pubsub.core.event.CounterUpdatedEvent
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.factory.EventBusFactory
import com.clouway.transactionlistener.adapter.firestore.FirestoreCounterUpdater
import com.clouway.transactionlistener.adapter.spark.CounterUpdatedEventHandler
import com.google.appengine.tools.cloudstorage.GcsFilename
import com.google.appengine.tools.cloudstorage.GcsServiceFactory
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.ServiceOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import spark.Spark.post
import spark.servlet.SparkApplication
import java.nio.channels.Channels

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppBootstrap : SparkApplication {
    override fun init() {

        val projectId = ServiceOptions.getDefaultProjectId()
        val transactionServiceUrl = "https://concurrent-test-dot-transactions-dot-$projectId.appspot.com"
        val pushUrl = "/_ah/push-handlers/pubsub/message"

        val eventBus = EventBusFactory.createAsyncPubsubEventBus()
        val updater = FirestoreCounterUpdater()

        val handlerMap = mapOf<Class<*>, EventHandler>(
                CounterUpdatedEvent::class.java to CounterUpdatedEventHandler(updater)
        )


        val firestoreServiceAccount = GcsFilename("sacred-union-210613.appspot.com",
                "spark-bankapp-firebase-adminsdk-e1srn-c06e915c42.json")

        val gscReadChannel = GcsServiceFactory
                .createGcsService()
                .openPrefetchingReadChannel(firestoreServiceAccount, 0, DEFAULT_BUFFER_SIZE)

        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(Channels.newInputStream(gscReadChannel)))
                .setDatabaseUrl("https://spark-bankapp.firebaseio.com/")
                .build()
        FirebaseApp.initializeApp(options)

        eventBus.subscribe("concurrent-counter",
                "concurrent-counter-listener",
                "$transactionServiceUrl$pushUrl")

        post(pushUrl, eventBus.register(handlerMap))

        post("/send") { req, _ ->
            val counterId = req.queryParams("counterId")
            val eventNum = req.queryParams("eventNum").toLong()
            publishEvents(eventBus, counterId, eventNum)
            200
        }

    }

    private fun publishEvents(eventBus: EventBus, counterId: String, eventNum: Long) {
        for (i in 0 until eventNum) {
                    eventBus.publish(CounterUpdatedEvent(counterId),
                            CounterUpdatedEvent::class.java,
                            "concurrent-counter")
        }
    }
}