package com.clouway.mailservice.adapter.gae

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
class PubsubReader {

    companion object {

        fun readMessage(input: InputStream): PubsubMessageWrapper{
            val stringBuilder = StringBuilder()

            BufferedReader(InputStreamReader(
                    input, Charset.forName(StandardCharsets.UTF_8.name()))).use {

                var next = 0
                fun getNext(): Int{
                    next = it.read()
                    return next
                }

                while(getNext() != -1){
                    stringBuilder.append(next.toChar())
                }
            }

            return Gson().fromJson(stringBuilder.toString(), PubsubMessageWrapper::class.java)
        }
    }

}