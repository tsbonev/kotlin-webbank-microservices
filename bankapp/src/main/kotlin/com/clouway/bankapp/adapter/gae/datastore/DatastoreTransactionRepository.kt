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
class DatastoreTransactionRepository(private val limit: Int = 100,
                                     private val getInstant:  () -> LocalDateTime = {LocalDateTime.now()}
) : TransactionRepository {

    private val TRANSACTION_KIND = "Transaction"

    private val service: DatastoreService
        get() = DatastoreServiceFactory.getDatastoreService()

    private fun getTransactionList(id: String, pageSize: Int = limit, offset: Int = 0): List<Transaction> {

        val transactionEntities = service
                .prepare(Query(TRANSACTION_KIND)
                        .setFilter(Query.FilterPredicate("accountId",
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
                transactionRequest.accountId,
                getInstant(),
                transactionRequest.amount
        )

        service.put(mapTransactionToEntity(transactionKey, transaction))
        return transaction
    }

    override fun getAccountTransactions(id: String, page: Int, pageSize: Int): List<Transaction> {
        return getTransactionList(id, pageSize, (page - 1) * pageSize)
    }

    override fun getAccountTransactions(id: String): List<Transaction> {
        return getTransactionList(id)
    }

    private fun mapEntityToTransaction(entity: Entity): Transaction{
        val typedEntity = TypedEntity(entity)
        return Transaction(
                typedEntity.string("id"),
                Operation.valueOf(typedEntity.string("operation")),
                typedEntity.string("accountId"),
                typedEntity.dateTimeValueOrNull("date")!!,
                typedEntity.double("amount")
        )
    }

    private fun mapTransactionToEntity(key: Key, transaction: Transaction): Entity{
        val typedEntity = TypedEntity(Entity(key))
        typedEntity.setUnindexedDateTimeValue("date", transaction.date)
        typedEntity.setUnindexedProperty("amount", transaction.amount)
        typedEntity.setIndexedProperty("operation", transaction.operation.name)
        typedEntity.setIndexedProperty("accountId", transaction.accountId)
        typedEntity.setIndexedProperty("id", transaction.id)
        return typedEntity.raw()
    }
}