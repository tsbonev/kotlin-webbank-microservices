package com.clouway.bankapp.core

import com.google.appengine.repackaged.com.google.gson.Gson

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class GsonSerializer :JsonSerializer {

    private val gson = Gson()

    override fun toJson(any: Any): String {
        return gson.toJson(any)
    }

    override fun <T> fromJson(string: String, typeOfT: Class<T>): T {
        return gson.fromJson(string, typeOfT)
    }
}