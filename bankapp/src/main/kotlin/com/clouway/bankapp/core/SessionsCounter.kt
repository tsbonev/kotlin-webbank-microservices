package com.clouway.bankapp.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface SessionsCounter {
    fun getActiveSessionsCount(): Int
}