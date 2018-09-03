package com.clouway.transactionlistener

import com.clouway.transactionlistener.adapter.firestore.FirestoreCounterUpdater
import com.clouway.transactionlistener.core.CounterUpdater
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import java.io.FileInputStream
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class FirebaseConnectionTest {

    private var db: Firestore

    private var counterUpdater: CounterUpdater

    init {
        val serviceAccount = FileInputStream("/home/tsvetozar/clouway/workspaces/idea/webbank-multiproject/transaction-listener/src/main/resources/spark-bankapp-firebase-adminsdk-e1srn-c06e915c42.json")

        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://spark-bankapp.firebaseio.com")
                .build()

        FirebaseApp.initializeApp(options)

        db = FirestoreClient.getFirestore()

        counterUpdater = FirestoreCounterUpdater { db }
    }
}