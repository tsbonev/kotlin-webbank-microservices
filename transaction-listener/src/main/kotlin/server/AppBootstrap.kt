package server

import com.clouway.pubsub.core.event.DepositMadeEvent
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.WithdrawMadeEvent
import com.clouway.pubsub.factory.EventBusFactory
import com.clouway.transactionlistener.adapter.firestore.FirestoreTransactionSaver
import com.clouway.transactionlistener.adapter.spark.DepositEventHandler
import com.clouway.transactionlistener.adapter.spark.WithdrawEventHandler
import com.google.appengine.tools.cloudstorage.GcsFilename
import com.google.appengine.tools.cloudstorage.GcsServiceFactory
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.ServiceOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import spark.Spark.post
import spark.servlet.SparkApplication
import java.nio.channels.Channels

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppBootstrap : SparkApplication {
    override fun init() {

        val projectId = ServiceOptions.getDefaultProjectId()
        val transactionServiceUrl = "https://transactions-dot-$projectId.appspot.com"
        val pushUrl = "/_ah/push-handlers/pubsub/message"

        val eventBus = EventBusFactory.createAsyncPubsubEventBus()
        val saver = FirestoreTransactionSaver()

        val handlerMap = mapOf<Class<*>, EventHandler>(
                DepositMadeEvent::class.java to DepositEventHandler(saver),
                WithdrawMadeEvent::class.java to WithdrawEventHandler(saver)
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

        eventBus.subscribe("user-transactions",
                "user-transaction-listener",
                "$transactionServiceUrl$pushUrl")

        post(pushUrl, eventBus.register(handlerMap))

    }
}