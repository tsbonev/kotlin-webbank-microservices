package com.clouway.bankapp.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface JsonSerializer {

    fun toJson(any: Any): String
    fun <T> fromJson(string: String, typeOfT: Class<T>): T
}