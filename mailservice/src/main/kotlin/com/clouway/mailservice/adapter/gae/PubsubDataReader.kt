package com.clouway.mailservice.adapter.gae

import com.clouway.mailservice.core.DataReader
import com.google.appengine.repackaged.com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class PubsubDataReader : DataReader {

    override fun readData(request: HttpServletRequest): Any {
        val pubsubMessage = readMessage(request.inputStream)
        return pubsubMessage.message.decodeData()
    }

    private fun readMessage(input: InputStream): PubsubMessageWrapper{
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