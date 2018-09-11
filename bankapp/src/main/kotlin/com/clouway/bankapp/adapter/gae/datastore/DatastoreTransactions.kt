package com.clouway.bankapp.adapter.gae.datastore

import com.clouway.bankapp.core.*
import com.clouway.bankapp.core.Transaction
import com.clouway.entityhelper.TypedEntity
import com.google.appengine.api.datastore.*
import com.google.appengine.api.datastore.FetchOptions.Builder.withLimit
import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreTransactions(private val limit: Int = 100,
                            private val getInstant:  () -> LocalDateTime = {LocalDateTime.now()}
) : Transactions {

    private val TRANSACTION_KIND = "Transaction"
    private val USER_KIND = "User"

    private val service: DatastoreService
        get() = DatastoreServiceFactory.getDatastoreService()

    private fun retrieveUsername(userId: String): String {
        val userKey = KeyFactory.createKey(USER_KIND, userId)
        return service.get(userKey).properties["username"].toString()
    }

    private fun getTransactionList(id: String, pageSize: Int = limit, offset: Int = 0): List<Transaction> {

        val transactionEntities = service
                .prepare(Query(TRANSACTION_KIND)
                        .setFilter(Query.FilterPredicate("userId",
                                Query.FilterOperator.EQUAL,
                                id)))
                .asList(withLimit(pageSize)
                        .offset(offset))

        val transactionList = mutableListOf<Transaction>()

        transactionEntities.forEach {
            transactionList.add(mapEntityToTransaction(it))
        }

        return transactionList
    }

    override fun save(transactionRequest: TransactionRequest): Transaction {

        val id = UUID.randomUUID().toString()

        val transactionKey = KeyFactory.createKey(TRANSACTION_KIND,
                id)

        val transaction = Transaction(
                id,
                transactionRequest.operation,
                transactionRequest.userId,
                getInstant(),
                transactionRequest.amount,
                retrieveUsername(transactionRequest.userId)
        )

        service.put(mapTransactionToEntity(transactionKey, transaction))
        return transaction
    }

    override fun getUserTransactions(id: String, page: Int, pageSize: Int): List<Transaction> {
        return getTransactionList(id, pageSize, (page - 1) * pageSize)
    }

    override fun getUserTransactions(id: String): List<Transaction> {
        return getTransactionList(id)
    }

    private fun mapEntityToTransaction(entity: Entity): Transaction{
        val typedEntity = TypedEntity(entity)
        return Transaction(
                typedEntity.string("id"),
                Operation.valueOf(typedEntity.string("operation")),
                typedEntity.string("userId"),
                typedEntity.dateTimeValueOrNull("date")!!,
                typedEntity.double("amount"),
                retrieveUsername(typedEntity.string("userId"))
        )
    }

    private fun mapTransactionToEntity(key: Key, transaction: Transaction): Entity{
        val typedEntity = TypedEntity(Entity(key))
        typedEntity.setUnindexedDateTimeValue("date", transaction.date)
        typedEntity.setUnindexedProperty("username", transaction.username)
        typedEntity.setUnindexedProperty("amount", transaction.amount)
        typedEntity.setIndexedProperty("operation", transaction.operation.name)
        typedEntity.setIndexedProperty("userId", transaction.userId)
        typedEntity.setIndexedProperty("id", transaction.id)
        return typedEntity.raw()
    }
}

