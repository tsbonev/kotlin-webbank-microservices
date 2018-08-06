package com.clouway.mailservice.adapter.gae

import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class PubsubMessage(val attributes: Any, val data: String, val message_id: String){
    fun decodeData(): String{
        return String(Base64.getDecoder().decode(data))
    }
}