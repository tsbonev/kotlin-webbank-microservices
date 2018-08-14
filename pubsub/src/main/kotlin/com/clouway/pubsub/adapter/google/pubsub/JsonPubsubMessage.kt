package com.clouway.pubsub.adapter.google.pubsub

import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class JsonPubsubMessage(val attributes: Map<String, String>, val data: String, val messageId: String){
    fun getEventType(): Class<*>{
        println(attributes["eventType"])
        return Class.forName(attributes["eventType"])
    }

    fun decodeData(): String{
        return String(Base64.getDecoder().decode(data))
    }
}