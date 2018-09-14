package com.clouway.transactionlistener.adapter.firestore

import com.clouway.transactionlistener.core.Transaction
import com.clouway.transactionlistener.core.TransactionSaver
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class FirestoreTransactionSaver(private val firestore: () -> Firestore = {FirestoreClient.getFirestore()})
    : TransactionSaver {

    override fun save(transaction: Transaction): Transaction {
        firestore().collection("transactions").add(transaction).get()
        return transaction
    }
}