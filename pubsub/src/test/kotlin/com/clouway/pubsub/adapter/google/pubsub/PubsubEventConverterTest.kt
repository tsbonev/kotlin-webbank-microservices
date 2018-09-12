package com.clouway.pubsub.adapter.google.pubsub

import com.google.appengine.repackaged.com.google.gson.Gson
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import org.junit.Test
import java.time.LocalDateTime
import kotlin.reflect.jvm.jvmName

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class PubsubEventConverterTest {

    private val instant = LocalDateTime.of(1, 1, 1, 1, 1, 1)
    private val jsonInstant = Gson().toJson(instant)
    private val eventConverter = PubsubEventConverter(getInstant = {instant})

    private val event = PubsubTopicTest.TestEvent("::data::")

    private val eventJson = """
        {"data":"::data::"}
    """.trimIndent()

    private val pubsubMessage = createTestPubsubMessage(eventJson)

    private fun createTestPubsubMessage(eventJson: String): PubsubMessage {
        val data = ByteString.copyFromUtf8(eventJson)
        return PubsubMessage.newBuilder().setData(data).putAttributes("eventType", PubsubTopicTest.TestEvent::class.jvmName)
                .putAttributes("time", jsonInstant).build()
    }


    @Test
    fun convertEventToPubsubMessage() {
        assertThat(eventConverter.convertEvent(event), Is(pubsubMessage))
    }

}