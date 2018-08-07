package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.EventConverter
import com.clouway.pubsub.core.event.Event
import com.google.appengine.repackaged.com.google.gson.Gson
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal class PubsubEventConverter : EventConverter<PubsubMessage> {
    override fun convertEvent(event: Event, eventType: Class<*>): PubsubMessage {
        val gson = Gson()
        val jsonEvent = gson.toJson(event)
        val data = ByteString.copyFromUtf8(jsonEvent)
        return PubsubMessage.newBuilder().setData(data).putAttributes("eventType", eventType.name).build()
    }
}