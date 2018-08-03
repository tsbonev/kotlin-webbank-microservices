package com.clouway.bankapp.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface TransactionRepository {

    fun save(transactionRequest: TransactionRequest)

    fun getUserTransactions(id: Long, page: Int, pageSize: Int): List<Transaction>
    fun getUserTransactions(id: Long): List<Transaction>
}