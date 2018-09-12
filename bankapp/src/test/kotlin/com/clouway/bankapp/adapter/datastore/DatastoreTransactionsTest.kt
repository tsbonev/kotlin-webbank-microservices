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
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreTransactionsTest {

    @Rule
    @JvmField
    val helper: DatastoreRule = DatastoreRule()

    private val transactions = DatastoreTransactions()
    private val transactionRequest = TransactionRequest("::id::", Operation.DEPOSIT, 200.0)

    @Before
    fun setUp() {
        val userEntity = Entity("User", "::id::")
        userEntity.setProperty("username", "::username::")
        DatastoreServiceFactory.getDatastoreService().put(userEntity)
    }

    @Test
    fun saveTransaction(){
        val savedTransaction = transactions.save(transactionRequest)

        assertThat(transactions.getUserTransactions("::id::").first(), Is(savedTransaction))
    }

    @Test
    fun retrieveUserTransactions(){
        val firstTransaction = transactions.save(transactionRequest)
        val secondTransaction = transactions.save(transactionRequest.copy(operation = Operation.WITHDRAW))

        val transactionList = listOf(firstTransaction, secondTransaction).sortedBy { it.id }

        assertThat(transactions.getUserTransactions("::id::").sortedBy { it.id }, Is(transactionList))
    }

    @Test
    fun returnEmptyTransactionList(){
        assertThat(transactions.getUserTransactions("::id::").isEmpty(), Is(true))
    }

    @Test
    fun paginateTransactions(){
        for(x in 1..10)
            transactions.save(
                    TransactionRequest("::id::",
                            Operation.DEPOSIT,
                            200.0
                    )
            )

        assertThat(transactions
                .getUserTransactions("::id::", 1, 2)
                .size, Is(2))

        assertThat(transactions
                .getUserTransactions("::id::", 4, 3)
                .size, Is(1))
    }
}