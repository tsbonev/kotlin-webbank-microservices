package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.*
import com.clouway.pubsub.core.event.Event
import com.google.appengine.repackaged.com.google.gson.Gson
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import org.jmock.AbstractExpectations.returnValue
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import spark.Request
import spark.Response
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.io.InputStream
import java.time.LocalDateTime
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.jvm.jvmName

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
@Suppress("UNCHECKED_CAST")
class PubsubTopicTest {

    data class TestEvent(val data: String) : Event

    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()

    private fun Mockery.expecting(block: Expectations.() -> Unit) {
        checking(Expectations().apply(block))
    }

    private val mockConverter: EventConverter<PubsubMessage> =
            context.mock(EventConverter::class.java) as EventConverter<PubsubMessage>

    private val mockPublisherProvider: PublisherProvider<PubsubMessage, Publisher> =
            context.mock(PublisherProvider::class.java) as PublisherProvider<PubsubMessage, Publisher>

    private val mockPublisherWrapper: PublisherWrapper<PubsubMessage, Publisher> =
            context.mock(PublisherWrapper::class.java) as PublisherWrapper<PubsubMessage, Publisher>

    private val topicName = "::topic::"

    private val testEvent = TestEvent("::data::")

    private val pubsubMessage = PubsubMessage.newBuilder().build()

    private val pubsubTopic = PubsubTopic(topicName, mockPublisherProvider, mockConverter)

    @Test
    fun shouldPublishPubsubMessage() {

        context.expecting {
            oneOf(mockConverter).convertEvent(testEvent)
            will(returnValue(pubsubMessage))

            oneOf(mockPublisherProvider).get(topicName)
            will(returnValue(mockPublisherWrapper))

            oneOf(mockPublisherWrapper).publish(pubsubMessage)
            oneOf(mockPublisherWrapper).shutdown()
        }

        pubsubTopic.publish(testEvent)
    }
}