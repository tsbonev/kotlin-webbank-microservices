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

    private val transformerWrapper = GsonSerializer()
    private val persistentSessionRepository = context.mock(SessionRepository::class.java)
    private val cachedSessionHandler = MemcacheSessionRepository(persistentSessionRepository, transformerWrapper)

    private val session = Session(1, "123SID", yesterday, "John",true)
    private val sessionRequest = SessionRequest(1, "123SID", "John", yesterday)


    @Test
    fun saveSessionInMemcache(){

        context.expecting {
            oneOf(persistentSessionRepository).issueSession(sessionRequest)
        }

        cachedSessionHandler.issueSession(sessionRequest)

        val retrievedSession = cachedSessionHandler.getSessionAvailableAt(session.sessionId, now)
        
        assertThat(retrievedSession.get().sessionId == session.sessionId, Is(true))
    }

    @Test(expected = SessionNotFoundException::class)
    fun removeSessionFromMemcache(){

        context.expecting {
            oneOf(persistentSessionRepository).issueSession(sessionRequest)
            oneOf(persistentSessionRepository).terminateSession(session.sessionId)
            oneOf(persistentSessionRepository).getSessionAvailableAt(session.sessionId, now)
            will(throwException(SessionNotFoundException()))
        }

        cachedSessionHandler.issueSession(sessionRequest)

        cachedSessionHandler.terminateSession(session.sessionId)

        cachedSessionHandler.getSessionAvailableAt(session.sessionId, now)
    }

}