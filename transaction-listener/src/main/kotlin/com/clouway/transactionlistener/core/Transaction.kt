package com.clouway.transactionlistener.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
enum class Operation{
    WITHDRAW, DEPOSIT
}

data class Transaction (val userId: String, val amount: Double, val date: Long, val operation: Operation)