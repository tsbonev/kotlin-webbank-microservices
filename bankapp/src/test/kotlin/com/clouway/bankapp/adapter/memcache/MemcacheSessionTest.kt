package com.clouway.bankapp.adapter.memcache

import com.clouway.bankapp.adapter.gae.memcache.MemcacheSessionRepository
import com.clouway.bankapp.core.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Test
import org.junit.Assert.assertThat
import org.junit.Rule
import rule.MemcacheRule
import org.jmock.AbstractExpectations.*
import java.time.LocalDateTime
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheSessionTest {

    @Rule
    @JvmField
    val helper: MemcacheRule = MemcacheRule()

    private fun Mockery.expecting(block: Expectations.() -> Unit){
            checking(Expectations().apply(block))
    }

    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()

    private val now = LocalDateTime.of(2018, 8, 2, 10, 36, 23, 905000000)
    private val yesterday = LocalDateTime.of(2018, 8, 1, 10, 36, 23, 905000000)

    private val serializer = GsonSerializer()
    private val persistentSessionRepository = context.mock(SessionRepository::class.java)
    private val cachedSessionHandler = MemcacheSessionRepository(persistentSessionRepository, serializer)

    private val testId = UUID.randomUUID().toString()

    private val session = Session(testId, "123SID", yesterday, "John",
            "email", listOf("::accountId::"), true)
    private val sessionRequest = SessionRequest(testId, "123SID", "John",
            "email", listOf("::accountId::"), yesterday)


    @Test
    fun saveSessionInMemcache(){

        context.expecting {
            oneOf(persistentSessionRepository).issueSession(sessionRequest)
        }

        cachedSessionHandler.issueSession(sessionRequest)

        val retrievedSession = cachedSessionHandler.getSessionAvailableAt(session.sessionId, now)
        
        assertThat(retrievedSession.get().sessionId == session.sessionId, Is(true))
    }

    @Test
    fun removeSessionFromMemcache(){

        context.expecting {
            oneOf(persistentSessionRepository).issueSession(sessionRequest)
            oneOf(persistentSessionRepository).terminateSession(session.sessionId)
            oneOf(persistentSessionRepository).getSessionAvailableAt(session.sessionId, now)
            will(returnValue(Optional.empty<Session>()))
        }

        cachedSessionHandler.issueSession(sessionRequest)

        cachedSessionHandler.terminateSession(session.sessionId)

        assertThat(cachedSessionHandler.getSessionAvailableAt(session.sessionId, now).isPresent, Is(false))
    }
}