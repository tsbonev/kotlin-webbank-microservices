package com.clouway.transactionlistener.adapter.firestore

import com.clouway.transactionlistener.core.CounterUpdater
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreException
import com.google.firebase.cloud.FirestoreClient
import org.eclipse.jetty.http.HttpStatus
import java.util.concurrent.ExecutionException


/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */

class FirestoreCounterUpdater(private val firestore: () -> Firestore = {FirestoreClient.getFirestore()})
    : CounterUpdater {

    override fun update(counterId: String): Int {

        return try{
            val transaction = firestore().runTransaction { transaction ->

                val docRef = firestore().document("counters/$counterId")

                val snapshot = transaction.get(docRef).get()
                val newCount = snapshot.getLong("count")!! + 1

                transaction.update(docRef, "count", newCount)
                newCount
            }
            transaction.get()
            HttpStatus.OK_200
        }catch (ex: FirestoreException){
            HttpStatus.UNPROCESSABLE_ENTITY_422
        }catch (ex: ExecutionException){
            HttpStatus.UNPROCESSABLE_ENTITY_422
        }
    }
}