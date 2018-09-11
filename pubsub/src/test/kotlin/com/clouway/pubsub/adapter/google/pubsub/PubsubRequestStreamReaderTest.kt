package com.clouway.pubsub.adapter.google.pubsub

import com.google.appengine.repackaged.com.google.gson.Gson
import org.junit.Test
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.io.InputStream
import java.time.LocalDateTime
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class PubsubRequestStreamReaderTest {


    private val reader = PubsubRequestStreamReader()

    private val instant = LocalDateTime.of(1, 1, 1, 1, 1, 1)
    private val jsonInstant = Gson().toJson(instant)

    private val requsestEncodedJson = """
        {
    "message": {
    "attributes": {
      "eventType": "${PubsubTopicTest.TestEvent::class.java.name}",
      "time": '$jsonInstant'
    },
    "data": "eyJkYXRhIjoiOjpkYXRhOjoifQ==",
    "message_id": "136969346945"
    },
    "subscription": "::subscriptions::/::subscription::"
    }
    """.trimIndent()

    private val messageStream: InputStream = requsestEncodedJson.byteInputStream()

    private val servletInputStream = object : ServletInputStream() {
        override fun setReadListener(readListener: ReadListener?) {
            throw NotImplementedException()
        }

        override fun isFinished(): Boolean {
            throw NotImplementedException()
        }

        override fun isReady(): Boolean {
            throw NotImplementedException()
        }

        override fun read(): Int {
            return messageStream.read()
        }
    }

    private val pubsubMessageWrapper = Gson().fromJson(requsestEncodedJson, PubsubMessageWrapper::class.java)

    @Test
    fun readAndParseRequestStream(){
        assertThat(reader.read(servletInputStream), Is(pubsubMessageWrapper))
    }
}