package com.clouway.loggingservice.adapter.spark

import com.clouway.loggingservice.core.Log
import com.clouway.loggingservice.core.Logger
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventWithAttributes
import org.eclipse.jetty.http.HttpStatus
import org.jmock.AbstractExpectations.throwException
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat


/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class LogEventHandlerTest {

    data class TestEvent(val data: String) : Event

    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()

    private fun Mockery.expecting(block: Expectations.() -> Unit){
            checking(Expectations().apply(block))
    }

    private val mockLogger = context.mock(Logger::class.java)

    private val event = TestEvent("::data::")

    private val instant = LocalDateTime.of(1, 1, 1, 1, 1, 1)

    private val log = Log(event, TestEvent::class.java, instant)

    private val logEventHandler = LogEventHandler(mockLogger)

    @Test
    fun shouldCallStoreLog(){

        context.expecting {
            oneOf(mockLogger).storeLog(log)
        }

        logEventHandler.handle(EventWithAttributes(event,
                mapOf(
                        "time" to instant,
                        "eventType" to event::class.java.name
                )))
    }

    @Test
    fun shouldReturnInternalErrorOnException(){
        context.expecting {
            oneOf(mockLogger).storeLog(log)
            will(throwException(Exception()))
        }

        assertThat(logEventHandler.handle(EventWithAttributes(event,
                mapOf(
                        "time" to instant,
                        "eventType" to event::class.java.name
                ))) as Int,
                Is(HttpStatus.INTERNAL_SERVER_ERROR_500))
    }
}