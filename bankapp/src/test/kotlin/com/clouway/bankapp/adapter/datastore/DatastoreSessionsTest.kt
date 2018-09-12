package com.clouway.bankapp.adapter.datastore

import com.clouway.bankapp.adapter.gae.datastore.DatastoreSessions
import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionRequest
import org.junit.Test
import org.junit.Assert.assertThat
import org.junit.Rule
import rule.DatastoreRule
import java.time.LocalDateTime
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class DatastoreSessionsTest {

    @Rule
    @JvmField
    val helper: DatastoreRule= DatastoreRule()

    private val instant = LocalDateTime.of(1, 2, 3, 1, 1, 1)
    private val refreshDays = 10L
    private val sessions = DatastoreSessions(getInstant = {instant}, sessionRefreshDays = refreshDays)

    private val activeSession = Session("::userId::", "::sid::", instant.plusDays(1), "::username::",
            "::email::", true)
    private val activeSessionRequest = SessionRequest("::userId::", "::sid::", "::username::",
            "::email::", instant.plusDays(refreshDays*2))
    private val expiredSessionRequest = SessionRequest("::userId::", "::sid-a::", "::username::",
            "::email::", instant.minusDays(2))
    @Test
    fun returnIssuedSession(){
        val issuedSession = sessions.issueSession(activeSessionRequest)

        assertThat(sessions.getSessionAvailableAt("::sid::", instant).isPresent, Is(true))
        assertThat(sessions.getSessionAvailableAt("::sid::", instant).get(), Is(issuedSession))
    }

    @Test
    fun returnEmptyWhenSessionIsExpired(){
        sessions.issueSession(expiredSessionRequest)

        assertThat(sessions.getSessionAvailableAt("::sid::", instant).isPresent, Is(false))
    }

    @Test
    fun deleteExpiredSession(){
        sessions.issueSession(expiredSessionRequest)
        sessions.deleteSessionsExpiringBefore(instant)

        assertThat(sessions.getSessionAvailableAt("::sid::", instant).isPresent, Is(false))
    }

    @Test
    fun terminateSession(){
        sessions.issueSession(activeSessionRequest)
        sessions.terminateSession(activeSession.sessionId)

        assertThat(sessions.getSessionAvailableAt("::sid::", instant).isPresent, Is(false))
    }

    @Test
    fun countActiveSessions(){
        sessions.issueSession(expiredSessionRequest)
        sessions.issueSession(activeSessionRequest)
        
        assertThat(sessions.getActiveSessionsCount(), Is(1))
    }

    @Test
    fun returnEmptyWhenSessionNotFound(){
        assertThat(sessions.
                getSessionAvailableAt("::fake-sid::", LocalDateTime.now())
                .isPresent, Is(false))
    }

    @Test
    fun refreshSession(){
        sessions.issueSession(activeSessionRequest.copy(expiration = instant))
        sessions.getSessionAvailableAt(activeSession.sessionId, instant)
        assertThat(sessions.getSessionAvailableAt(activeSession.sessionId, instant)
                .get().expiresOn, Is(instant.plusDays(refreshDays)))
    }

    @Test
    fun refreshOnlyWhenBelowRefreshDaysInLength(){
        sessions.issueSession(activeSessionRequest.copy(expiration = instant.plusDays(refreshDays*2)))
        sessions.getSessionAvailableAt(activeSession.sessionId, instant)
        assertThat(sessions.getSessionAvailableAt(activeSession.sessionId, instant)
                .get().expiresOn, Is(instant.plusDays(refreshDays*2)))
    }
}