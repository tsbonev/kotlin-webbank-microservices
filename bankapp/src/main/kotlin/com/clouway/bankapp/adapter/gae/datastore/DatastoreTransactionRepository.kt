package com.clouway.bankapp.adapter.gae.datastore

import com.clouway.bankapp.core.*
import com.clouway.bankapp.core.Transaction
import com.google.appengine.api.datastore.*
import com.google.appengine.api.datastore.FetchOptions.Builder.withLimit
import java.time.LocalDateTime
import java.util.*
import kotlin.math.absoluteValue

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreTransactionRepository(private val limit: Int = 100,
                                     private val instant: LocalDateTime = LocalDateTime.now()
) : TransactionRepository {

    private fun mapEntityToTransaction(entity: Entity): Transaction{
        val typedEntity = TypedEntity(entity)
        return Transaction(
                typedEntity.longValue("id"),
                Operation.valueOf(typedEntity.string("operation")),
                typedEntity.longValue("userId"),
                typedEntity.dateTimeValueOrNull("date")!!,
                typedEntity.double("amount"),
                retrieveUsername(typedEntity.longValue("userId"))
        )
    }

    private fun mapTransactionToEntity(key: Key, transaction: Transaction): Entity{
        val typedEntity = TypedEntity(Entity(key))
        typedEntity.setUnindexedDateTimeValue("date", transaction.date)
        typedEntity.setUnindexedProperty("userId", transaction.username)
        typedEntity.setUnindexedProperty("amount", transaction.amount)
        typedEntity.setIndexedProperty("operation", transaction.operation.name)
        typedEntity.setIndexedProperty("userId", transaction.userId)
        typedEntity.setIndexedProperty("id", transaction.id)
        return typedEntity.raw()
    }

    private val service: DatastoreService
        get() = DatastoreServiceFactory.getDatastoreService()

    private fun retrieveUsername(userId: Long): String {
        val userKey = KeyFactory.createKey("User", userId)
        return service.get(userKey).properties["username"].toString()
    }

    private fun andFilter(param: String, value: Long): Query.Filter {
        return Query.FilterPredicate(param,
                Query.FilterOperator.EQUAL, value)
    }

    private fun getTransactionList(id: Long, pageSize: Int = limit, offset: Int = 0): List<Transaction> {

        val transactionEntities = service
                .prepare(Query("Transaction")
                        .setFilter(andFilter("userId", id)))
                .asList(withLimit(pageSize)
                        .offset(offset))

        val transactionList = mutableListOf<Transaction>()

        transactionEntities.forEach {
            transactionList.add(mapEntityToTransaction(it))
        }

        return transactionList
    }

    override fun save(transactionRequest: TransactionRequest) {

        val transactionKey = KeyFactory.createKey("Transaction",
                UUID.randomUUID()
                        .leastSignificantBits
                        .absoluteValue)

        val transaction = Transaction(
                transactionKey.id,
                transactionRequest.operation,
                transactionRequest.userId,
                instant,
                transactionRequest.amount,
                retrieveUsername(transactionRequest.userId)
        )

        service.put(mapTransactionToEntity(transactionKey, transaction))
    }

    override fun getUserTransactions(id: Long, page: Int, pageSize: Int): List<Transaction> {
        return getTransactionList(id, pageSize, (page - 1) * pageSize)
    }

    override fun getUserTransactions(id: Long): List<Transaction> {
        return getTransactionList(id)
    }
}