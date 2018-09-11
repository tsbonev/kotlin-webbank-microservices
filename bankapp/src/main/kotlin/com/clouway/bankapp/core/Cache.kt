package com.clouway.bankapp.core

import java.util.Optional

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface Cache<T> {
    fun put(obj:  T): T
    fun get(key: String): Optional<T>
    fun remove(key: String)
}