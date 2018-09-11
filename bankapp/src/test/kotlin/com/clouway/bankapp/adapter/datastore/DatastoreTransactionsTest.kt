package com.clouway.bankapp.adapter.datastore

import com.clouway.bankapp.adapter.gae.datastore.DatastoreTransactions
import com.clouway.bankapp.core.*
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertThat
import org.junit.Rule
import rule.DatastoreRule
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreTransactionsTest {

    @Rule
    @JvmField
    val helper: DatastoreRule = DatastoreRule()

    private val transactionRepo = DatastoreTransactions()
    private val testId = UUID.randomUUID().toString()
    private val transactionRequest = TransactionRequest(testId, Operation.DEPOSIT, 200.0)

    @Before
    fun setUp() {
        val userEntity = Entity("User", testId)
        userEntity.setProperty("username", "John")
        DatastoreServiceFactory.getDatastoreService().put(userEntity)
    }

    @Test
    fun shouldSaveTransaction(){

        transactionRepo.save(transactionRequest)

        assertThat(transactionRepo.getUserTransactions(testId).isNotEmpty(), Is(true))
    }

    @Test
    fun shouldReturnEmptyTransactionList(){

        assertThat(transactionRepo.getUserTransactions(testId).isEmpty(), Is(true))

    }

    @Test
    fun shouldPaginateTransactions(){

        for(x in 1..10)
            transactionRepo.save(
                    TransactionRequest(testId,
                            Operation.DEPOSIT,
                            200.0
                    )
            )

        assertThat(transactionRepo
                .getUserTransactions(testId, 1, 2)
                .size, Is(2))

        assertThat(transactionRepo
                .getUserTransactions(testId, 4, 3)
                .size, Is(1))
    }
}