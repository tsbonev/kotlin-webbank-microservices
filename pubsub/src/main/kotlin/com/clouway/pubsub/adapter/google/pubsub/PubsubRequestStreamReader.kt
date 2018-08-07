package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.RequestStreamReader
import com.google.appengine.repackaged.com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal class PubsubRequestStreamReader : RequestStreamReader<PubsubMessageWrapper> {
    override fun read(requestStreamInput: InputStream): PubsubMessageWrapper {
        val gson = Gson()

        val stringBuilder = StringBuilder()

        BufferedReader(InputStreamReader(
                requestStreamInput, Charset.forName(StandardCharsets.UTF_8.name()))).use {

            var next = 0
            fun getNext(): Int{
                next = it.read()
                return next
            }

            while(getNext() != -1){
                stringBuilder.append(next.toChar())
            }
        }

        return gson.fromJson(stringBuilder.toString(), PubsubMessageWrapper::class.java)
    }
}