package com.clouway.bankapp.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class TransactionRequest ( val userId: String, val operation: Operation, val amount: Double)