package com.clouway.bankapp.adapter.mongodb

import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.Transaction
import com.clouway.bankapp.core.TransactionRepository
import com.clouway.bankapp.core.TransactionRequest
import com.clouway.entityhelper.toUtilDate
import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Indexes
import org.bson.Document
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MongoTransactionRepository(private val dbName: String,
                                 private val client: MongoClient,
                                 private val getInstant: () -> LocalDateTime = { LocalDateTime.now() }) : TransactionRepository {

    private val TRANSACTION_COLLECTION = "Transactions"

    /**
     * "A MongoDB client with internal connection pooling.
     * For most applications, you should have one MongoClient instance for the entire JVM."
     * ref: http://mongodb.github.io/mongo-java-driver/3.6/javadoc/com/mongodb/MongoClient.html
     */
    private val collection: MongoCollection<Document>
        get() = client.getDatabase(dbName).getCollection(TRANSACTION_COLLECTION)

    /**
     * Creates a index on the userId field.
     */
    init {
        collection.createIndex(Indexes.ascending("userId"))
    }

    override fun save(transactionRequest: TransactionRequest): Transaction {
        val transaction = Transaction(
                UUID.randomUUID().toString(),
                transactionRequest.operation,
                transactionRequest.userId,
                getInstant(),
                transactionRequest.amount,
                retrieveUsername(transactionRequest.userId)
        )
        collection.insertOne(mapTransactionToDocument(transaction))
        return transaction
    }

    override fun getUserTransactions(id: String, page: Int, pageSize: Int): List<Transaction> {
        val transactionList = mutableListOf<Transaction>()
        (
                collection.find(eq("userId", id)).skip((page - 1) * pageSize)
                        .limit(pageSize)
                        .forEach {
                            transactionList.add(mapDocumentToTransaction(it))
                        }
                )
        return transactionList
    }

    override fun getUserTransactions(id: String): List<Transaction> {
        val transactionList = mutableListOf<Transaction>()
        (
                collection.find(eq("userId", id)).forEach {
                    transactionList.add(mapDocumentToTransaction(it))
                }
                )
        return transactionList
    }

    private fun retrieveUsername(id: String): String {
        return MongoUserRepository(dbName, client).getById(id).get().username
    }

    private fun mapTransactionToDocument(transaction: Transaction): Document {
        val document = Document()
        document.append("_id", transaction.id)
        document.append("userId", transaction.userId)
        document.append("username", transaction.username)
        document.append("amount", transaction.amount)
        document.append("date", transaction.date.toUtilDate())
        document.append("operation", transaction.operation.toString())
        return document
    }

    private fun mapDocumentToTransaction(document: Document): Transaction {
        return Transaction(
                document.getString("_id"),
                Operation.valueOf(document.getString("operation")),
                document.getString("userId"),
                LocalDateTime.ofInstant(document.getDate("date").toInstant(), ZoneOffset.UTC),
                document.getDouble("amount"),
                document.getString("username")
        )
    }
}