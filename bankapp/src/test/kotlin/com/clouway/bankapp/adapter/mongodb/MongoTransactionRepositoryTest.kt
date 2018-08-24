package com.clouway.bankapp.adapter.mongodb

import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.Transaction
import com.clouway.bankapp.core.TransactionRequest
import com.github.fakemongo.junit.FongoRule
import com.mongodb.MongoClient
import org.bson.Document
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MongoTransactionRepositoryTest {

    @Rule
    @JvmField
    val fongoRule = FongoRule()

    lateinit var mongoClient: MongoClient

    lateinit var transactionRepo: MongoTransactionRepository

    private val now = LocalDateTime.of(2018, 8, 2, 10, 36, 23, 905000000)

    private val testId = UUID.randomUUID().toString()
    private val transactionRequest = TransactionRequest(testId, Operation.DEPOSIT, 200.0)
    private val transaction = Transaction(testId,
            Operation.DEPOSIT,
            testId,
            now,
            200.0,
            "::username::"
    )

    @Before
    fun setUp(){
        mongoClient = fongoRule.mongoClient
        mongoClient.getDatabase("test").getCollection("Users").insertOne(
                Document(mapOf(
                        "_id" to testId,
                        "username" to "::username::",
                        "email" to "::email::",
                        "password" to "::password::"
                ))
        )
        transactionRepo = MongoTransactionRepository("test",
                mongoClient,
                getInstant = {now})
    }

    @Test
    fun shouldSaveTransaction(){
        val savedTransaction = transactionRepo.save(transactionRequest)

        assertThat(savedTransaction.username, Is(transaction.username))
        assertThat(savedTransaction.userId, Is(transaction.userId))
        assertThat(savedTransaction.amount, Is(transaction.amount))
        assertThat(savedTransaction.operation, Is(transaction.operation))
        assertThat(savedTransaction.date, Is(transaction.date))
    }

    @Test
    fun shouldRetrieveTransactions(){
        val firstTransaction = transactionRepo.save(transactionRequest)
        val secondTransaction = transactionRepo.save(transactionRequest)

        assertThat(transactionRepo.getUserTransactions(testId), Is(listOf(
                firstTransaction,
                secondTransaction
        )))
    }

    @Test
    fun shouldPaginateTransactions(){
        val firstTransaction = transactionRepo.save(transactionRequest)
        val secondTransaction = transactionRepo.save(transactionRequest)
        val thirdTransaction = transactionRepo.save(transactionRequest)

        assertThat(transactionRepo.getUserTransactions(testId, 2, 1), Is(listOf(
                secondTransaction
        )))
    }

    @Test
    fun shouldReturnNoTransactions(){
        assertThat(transactionRepo.getUserTransactions(testId, 2, 1), Is(emptyList()))
    }

}