package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.*
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
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
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.jvm.jvmName

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */

data class TestEvent(val data: String) : Event

@Suppress("UNCHECKED_CAST")
class GooglePubsubTest {

    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()

    private fun Mockery.expecting(block: Expectations.() -> Unit) {
        checking(Expectations().apply(block))
    }

    private val mockConverter: EventConverter<PubsubMessage> =
            context.mock(EventConverter::class.java) as EventConverter<PubsubMessage>

    private val mockReader: RequestStreamReader<PubsubMessageWrapper> =
            context.mock(RequestStreamReader::class.java) as RequestStreamReader<PubsubMessageWrapper>

    private val mockPublisherProvider: PublisherProvider<PubsubMessage, Publisher> =
            context.mock(PublisherProvider::class.java) as PublisherProvider<PubsubMessage, Publisher>

    private val mockPublisherWrapper: PublisherWrapper<PubsubMessage, Publisher> =
            context.mock(PublisherWrapper::class.java) as PublisherWrapper<PubsubMessage, Publisher>

    private val mockEventHandler = context.mock(EventHandler::class.java)

    private val testEventBus = AsyncPubsubEventBus(mockPublisherProvider, mockConverter, mockReader)

    private val testTopic = "::topic::"
    private val testEvent = TestEvent("::data::")

    private val testEventJson = """
        {"data":"::data::"}
    """.trimIndent()
    private val converter = PubsubEventConverter()

    private val testPubsubMessage = createTestPubsubMessage(testEventJson)

    private fun createTestPubsubMessage(eventJson: String): PubsubMessage {
        val data = ByteString.copyFromUtf8(eventJson)
        return PubsubMessage.newBuilder().setData(data).putAttributes("eventType", TestEvent::class.jvmName).build()
    }

    private val requsestEncodedJson = """
        {
    "message": {
    "attributes": {
      "eventType": "${TestEvent::class.java.name}"
    },
    "data": "eyJkYXRhIjoiOjpkYXRhOjoifQ==",
    "message_id": "136969346945"
    },
    "subscription": "::subscriptions::/::subscription::"
    }
    """.trimIndent()

    private val pubsubMessageWrapper = Gson().fromJson(requsestEncodedJson, PubsubMessageWrapper::class.java)

    private val testRequestDecoder = PubsubRequestStreamReader()

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

    private val mockServletRequest = context.mock(HttpServletRequest::class.java)

    private val fakeRequest = object : Request() {

        override fun attribute(attribute: String?, value: Any?) {
        }

        override fun raw(): HttpServletRequest {
            return mockServletRequest
        }
    }

    private val fakeResponse = object : Response(){

    }

    @Test
    fun shouldConvertEventToPubsubMesssage() {
        assertThat(converter.convertEvent(testEvent, TestEvent::class.java), Is(testPubsubMessage))
    }

    @Test
    fun shouldPublishPubsubMessage() {

        context.expecting {
            oneOf(mockConverter).convertEvent(testEvent, TestEvent::class.java)
            will(returnValue(testPubsubMessage))
            oneOf(mockPublisherProvider).get(testTopic)
            will(returnValue(mockPublisherWrapper))
            oneOf(mockPublisherWrapper).publish(testPubsubMessage)
            oneOf(mockPublisherWrapper).shutdown()
        }

        testEventBus.publish(testEvent, TestEvent::class.java, testTopic)
    }

    @Test
    fun shouldRegisterEventHandler(){
        
        val handlerMap = mapOf<Class<*>, EventHandler>(
                TestEvent::class.java to mockEventHandler
        )

        context.expecting {
            oneOf(mockEventHandler).handle(fakeRequest, fakeResponse)

            oneOf(mockServletRequest).inputStream
            will(returnValue(servletInputStream))

            oneOf(mockReader).read(servletInputStream)
            will(returnValue(pubsubMessageWrapper))
        }

        testEventBus.register(handlerMap).handle(fakeRequest, fakeResponse)
    }

    @Test
    fun shouldConvertAndPublishEvent() {

        context.expecting {
            oneOf(mockConverter).convertEvent(testEvent, TestEvent::class.java)
            will(returnValue(testPubsubMessage))
            oneOf(mockPublisherProvider).get(testTopic)
            will(returnValue(mockPublisherWrapper))
            oneOf(mockPublisherWrapper).publish(testPubsubMessage)
            oneOf(mockPublisherWrapper).shutdown()
        }

        testEventBus.publish(testEvent, TestEvent::class.java, testTopic)
    }

    @Test
    fun shouldDecodeRequestStream() {

        val testEvent = TestEvent("::data::")

        val decodedEvent = Gson().fromJson(testRequestDecoder
                .read(servletInputStream)
                .message
                .decodeData(), TestEvent::class.java)

        assertThat(decodedEvent,
                Is(testEvent))
    }
}