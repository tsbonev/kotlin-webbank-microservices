package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.RequestStreamReader
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.EventWithAttributes
import com.google.appengine.repackaged.com.google.gson.Gson
import org.jmock.AbstractExpectations.returnValue
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.io.InputStream
import java.time.LocalDateTime
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
@Suppress("UNCHECKED_CAST")
class PubsubSubscriptionTest {

    data class TestEvent(val data: String) : Event

    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()

    private fun Mockery.expecting(block: Expectations.() -> Unit) {
        checking(Expectations().apply(block))
    }

    private val streamReader = context.mock(RequestStreamReader::class.java) as RequestStreamReader<PubsubMessageWrapper>

    private val subscription = PubsubSubscription("::subscription-name::",
            "::topic-name::",
            "::endpoint::",
            streamReader
    )

    private val instant = LocalDateTime.of(1, 1, 1, 1, 1, 1)
    private val jsonInstant = Gson().toJson(instant)

    private val requsestEncodedJson = """
        {
    "message": {
    "attributes": {
      "eventType": "${TestEvent::class.java.name}",
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

    private val servletRequest = context.mock(HttpServletRequest::class.java)
    private val servletResponse = context.mock(HttpServletResponse::class.java)

    @Test
    fun handleEvent() {
        subscription.registerEventHandler(TestEvent::class.java, TestEventHandler())

        context.expecting {
            oneOf(servletRequest).inputStream
            will(returnValue(servletInputStream))
            oneOf(streamReader).read(servletInputStream)
            will(returnValue(pubsubMessageWrapper))
        }

        assertThat(subscription.handle(servletRequest, servletResponse) as Int, Is(200))
    }

    class TestEventHandler : EventHandler {
        override fun handle(eventWithAttributes: EventWithAttributes): Any? {
            return 200
        }
    }
}