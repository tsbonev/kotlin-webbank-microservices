package com.clouway.transactionlistener

import com.clouway.transactionlistener.adapter.firestore.FirestoreTransactionSaver
import com.clouway.transactionlistener.core.Operation
import com.clouway.transactionlistener.core.Transaction
import com.clouway.transactionlistener.core.TransactionSaver
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import org.junit.After
import org.junit.Test
import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class FirebaseConnectionTest {

    private var db: Firestore

    private var transactionSaver: TransactionSaver

    init {
        val serviceAccount = FileInputStream("/home/tsvetozar/clouway/workspaces/idea/webbank-multiproject/transaction-listener/src/main/resources/spark-bankapp-firebase-adminsdk-e1srn-c06e915c42.json")

        val options = FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://spark-bankapp.firebaseio.com")
                .build()

        FirebaseApp.initializeApp(options)

        db = FirestoreClient.getFirestore()

        transactionSaver = FirestoreTransactionSaver {db}
    }

    private val testIdsToDelete = mutableListOf<String>()

    private val transaction = Transaction(
            "::userId::",
            100.0,
            LocalDateTime.now().toInstant(ZoneOffset.UTC).epochSecond,
            Operation.WITHDRAW
    )

    @After
    fun cleanUp(){
        val coll = db.collection("transactions")
        testIdsToDelete.forEach{
            coll.document(it).delete()
        }
        testIdsToDelete.clear()
    }

    @Test
    fun saveTransactionToFirestore(){
        transactionSaver.save(transaction)
        val transactions = db.collection("transactions")
        val query = transactions.whereEqualTo("userId", "::userId::").get()
        val documents = query.get().documents
        assertThat(documents.size, Is(1))
        testIdsToDelete.add(documents.first().id)
    }

    @Test
    fun addSnapshotListener(){
        val docRef = db.collection("transactions").document()

        docRef.addSnapshotListener { snapshot, error ->
            if(error != null){
                println("Listen failed: $error")
                return@addSnapshotListener
            }
            if(snapshot != null && snapshot.exists()){
                println("Current data: ${snapshot.data}")
            }else{
                println("Current data: null")
            }
        }

        docRef.set(transaction).get()
        docRef.update("userId", "::newUserId::").get()
        docRef.update("operation", Operation.DEPOSIT.toString()).get()
        testIdsToDelete.add(docRef.id)
    }

}