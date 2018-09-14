package com.clouway.transactionlistener.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface TransactionSaver {

    fun save(transaction: Transaction): Transaction

}