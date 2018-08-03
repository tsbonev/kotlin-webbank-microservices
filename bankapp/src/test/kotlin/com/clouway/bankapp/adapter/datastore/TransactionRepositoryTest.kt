package com.clouway.bankapp.adapter.datastore

import com.clouway.bankapp.adapter.gae.datastore.DatastoreTransactionRepository
import com.clouway.bankapp.core.*
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertThat
import org.junit.Rule
import rule.DatastoreRule
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class TransactionRepositoryTest {

    @Rule
    @JvmField
    val helper: DatastoreRule = DatastoreRule()

    private val transactionRepo = DatastoreTransactionRepository()
    private val transactionRequest = TransactionRequest(1, Operation.DEPOSIT, 200.0)
    private val userJson = """
            {
            "id"=1,
            "username"="John",
            "password"="password"
            }
        """.trimIndent()

    @Before
    fun setUp() {
        val userEntity = Entity("User", 1)
        userEntity.setProperty("username", "John")
        userEntity.setProperty("content", userJson)
        DatastoreServiceFactory.getDatastoreService().put(userEntity)
    }

    @Test
    fun shouldSaveTransaction(){

        transactionRepo.save(transactionRequest)

        assertThat(transactionRepo.getUserTransactions(1).isNotEmpty(), Is(true))
    }

    @Test
    fun shouldReturnEmptyTransactionList(){

        assertThat(transactionRepo.getUserTransactions(1).isEmpty(), Is(true))

    }

    @Test
    fun shouldPaginateTransactions(){

        for(x in 1..10)
            transactionRepo.save(
                    TransactionRequest(1,
                            Operation.DEPOSIT,
                            200.0
                    )
            )

        assertThat(transactionRepo
                .getUserTransactions(1, 1, 2)
                .size, Is(2))

        assertThat(transactionRepo
                .getUserTransactions(1, 4, 3)
                .size, Is(1))
    }

}