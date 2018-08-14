package com.clouway.pubsub.adapter.google.pubsub

import com.google.appengine.repackaged.com.google.gson.Gson
import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class JsonPubsubMessage(val attributes: Map<String, String>, val data: String, val messageId: String){

    fun getTime(): LocalDateTime {
        val gson = Gson()
        return gson.fromJson(attributes["time"], LocalDateTime::class.java)
    }

    fun getEventType(): Class<*>{
        return Class.forName(attributes["eventType"])
    }

    fun decodeData(): String{
        return String(Base64.getDecoder().decode(data))
    }
}