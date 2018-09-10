package com.clouway.transactionlistener.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface CounterUpdater {
    fun update(counterId: String): Int
}