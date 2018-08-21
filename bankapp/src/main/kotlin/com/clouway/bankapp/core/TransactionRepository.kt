package com.clouway.bankapp.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface TransactionRepository {

    fun save(transactionRequest: TransactionRequest): Transaction

    fun getAccountTransactions(id: String, page: Int, pageSize: Int): List<Transaction>
    fun getAccountTransactions(id: String): List<Transaction>
}