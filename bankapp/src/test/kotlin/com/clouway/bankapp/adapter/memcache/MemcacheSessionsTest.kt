package com.clouway.bankapp.adapter.memcache
import com.clouway.bankapp.adapter.gae.memcache.MemcacheSessions
import com.clouway.bankapp.core.*
import org.junit.Test
import org.junit.Assert.assertThat
import org.junit.Rule
import rule.MemcacheRule
import java.time.LocalDateTime
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is
/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MemcacheSessionsTest {
    @Rule
    @JvmField
    val helper: MemcacheRule = MemcacheRule()

    private val instant = LocalDateTime.of(1, 1, 1, 1, 1, 1)
    private val sessionService = MemcacheSessions()
    private val session = Session("::userId::", "::sid::", instant, ":username:",
            "::user-email::", true)
    @Test
    fun saveSessionToMemcache(){
        assertThat(sessionService.put(session), Is(session))
    }
    @Test
    fun retrieveSessionFromMemcache(){
        sessionService.put(session)
        val retrievedSession = sessionService.get(session.sessionId)
        assertThat(retrievedSession.get(), Is(session))
    }
    @Test
    fun removeSessionFromMemcache(){
        sessionService.put(session)
        sessionService.remove(session.sessionId)
        assertThat(sessionService.get(session.sessionId).isPresent,
                Is(false))
    }
}