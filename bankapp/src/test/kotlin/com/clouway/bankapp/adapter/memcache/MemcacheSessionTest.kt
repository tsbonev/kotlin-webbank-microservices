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
    private val tomorrow = LocalDateTime.of(2018, 8, 3, 10, 36, 23, 905000000)

    private val mockPersistentSessionRepository = context.mock(SessionRepository::class.java)
    private val memcacheSessionRepository = MemcacheSessionRepository(mockPersistentSessionRepository)

    private val testId = UUID.randomUUID().toString()

    private val session = Session(testId, "123SID", tomorrow, "John",
            "email", true)
    private val sessionRequest = SessionRequest(testId, "123SID", "John",
            "email", tomorrow)

    @Test
    fun shouldSaveSessionInMemcache(){

        context.expecting {
            oneOf(mockPersistentSessionRepository).issueSession(sessionRequest)
            will(returnValue(session))
        }

        memcacheSessionRepository.issueSession(sessionRequest)

        val retrievedSession = memcacheSessionRepository.getSessionAvailableAt(session.sessionId, now)
        
        assertThat(retrievedSession.get(), Is(session))
    }

    @Test
    fun shouldNotReturnExpiredSession(){
        val expiredSession = Session(testId, session.sessionId, now.minusDays(5), "John",
                "email")
        val expiredSessionRequest = SessionRequest(testId, session.sessionId, "John",
                "email", now.minusDays(5))

        context.expecting {
            oneOf(mockPersistentSessionRepository).issueSession(expiredSessionRequest)
            will(returnValue(expiredSession))

            oneOf(mockPersistentSessionRepository).terminateSession(session.sessionId)
        }

        memcacheSessionRepository.issueSession(expiredSessionRequest)
        val retrievedSession = memcacheSessionRepository.getSessionAvailableAt(session.sessionId, now)
        assertThat(retrievedSession.isPresent, Is(false))
    }

    @Test
    fun shouldRemoveSessionFromMemcache(){

        context.expecting {
            oneOf(mockPersistentSessionRepository).issueSession(sessionRequest)
            will(returnValue(session))

            oneOf(mockPersistentSessionRepository).terminateSession(session.sessionId)

            oneOf(mockPersistentSessionRepository).getSessionAvailableAt(session.sessionId, now)
            will(returnValue(Optional.empty<Session>()))
        }

        memcacheSessionRepository.issueSession(sessionRequest)

        memcacheSessionRepository.terminateSession(session.sessionId)

        assertThat(memcacheSessionRepository.getSessionAvailableAt(session.sessionId, now).isPresent,
                Is(false))
    }
}