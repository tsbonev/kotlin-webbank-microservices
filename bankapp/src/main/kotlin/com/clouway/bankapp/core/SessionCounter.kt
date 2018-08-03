package com.clouway.bankapp.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface SessionCounter {
    fun getActiveSessionsCount(): Int
}