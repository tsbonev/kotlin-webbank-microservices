package com.clouway.loggingservice.adapter.spark

import com.clouway.loggingservice.core.Log
import com.clouway.loggingservice.core.Logger
import com.clouway.pubsub.core.event.Event
import org.eclipse.jetty.http.HttpStatus
import org.jmock.AbstractExpectations.throwException
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Rule
import org.junit.Test
import spark.Request
import spark.Response
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

    private val fakeRequest = object : Request(){

        override fun <T : Any?> attribute(attribute: String?): T {
            if(attribute == "event") return testEvent as T
            if(attribute == "eventType") return TestEvent::class.java.name as T
            if(attribute == "time") return testInstant as T
            throw Exception()
        }
    }

    private val fakeResponse = object : Response(){

    }

    private val testEvent = TestEvent("::data::")

    private val testInstant = LocalDateTime.of(1, 1, 1, 1, 1, 1)

    private val testLog = Log(testEvent, TestEvent::class.java, testInstant)

    private val logEventHandler = LogEventHandler(mockLogger)

    @Test
    fun shouldCallStoreLog(){

        context.expecting {
            oneOf(mockLogger).storeLog(testLog)
        }

        logEventHandler.handle(fakeRequest, fakeResponse)
    }

    @Test
    fun shouldReturnInternalErrorOnException(){
        context.expecting {
            oneOf(mockLogger).storeLog(testLog)
            will(throwException(Exception()))
        }

        assertThat(logEventHandler.handle(fakeRequest, fakeResponse) as Int,
                Is(HttpStatus.INTERNAL_SERVER_ERROR_500))
    }
}