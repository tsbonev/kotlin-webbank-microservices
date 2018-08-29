package com.clouway.bankapp.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface TransactionRepository {

    fun save(transactionRequest: TransactionRequest): Transaction

    fun getUserTransactions(id: String, page: Int, pageSize: Int): List<Transaction>
    fun getUserTransactions(id: String): List<Transaction>
}