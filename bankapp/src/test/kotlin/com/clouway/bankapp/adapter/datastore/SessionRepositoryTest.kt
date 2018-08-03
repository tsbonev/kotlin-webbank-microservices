package com.clouway.bankapp.adapter.datastore

import com.clouway.bankapp.adapter.gae.datastore.DatastoreSessionRepository
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
class SessionRepositoryTest {

    @Rule
    @JvmField
    val helper: DatastoreRule= DatastoreRule()

    private val tomorrow =  LocalDateTime.of(2018, 8, 3, 10, 36, 23, 905000000)
    private val now = LocalDateTime.of(2018, 8, 2, 10, 36, 23, 905000000)
    private val yesterday =  LocalDateTime.of(2018, 8, 1, 10, 36, 23, 905000000)
    private val sessionRepo = DatastoreSessionRepository(instant = now, sessionRefreshDays = 1)

    private val activeSession = Session(1, "123", tomorrow, "John", true)
    private val activeSessionRequest = SessionRequest(1, "123", "John", tomorrow)
    private val expiredSessionRequest = SessionRequest(1, "1234", "John", yesterday)
    @Test
    fun shouldRegisterSession(){

        sessionRepo.issueSession(activeSessionRequest)

        assertThat(sessionRepo.getSessionAvailableAt("123", now).isPresent, Is(true))

    }

    @Test
    fun shouldNotGetExpiredSession(){

        sessionRepo.issueSession(expiredSessionRequest)

        assertThat(sessionRepo.getSessionAvailableAt("123", now).isPresent, Is(false))

    }

    @Test
    fun shouldDeleteExpiringSession(){

        sessionRepo.issueSession(expiredSessionRequest)
        sessionRepo.deleteSessionsExpiringBefore(now)

        assertThat(sessionRepo.getSessionAvailableAt("123", now).isPresent, Is(false))

    }

    @Test
    fun shouldTerminateSession(){

        sessionRepo.issueSession(activeSessionRequest)
        sessionRepo.terminateSession(activeSession.sessionId)

        assertThat(sessionRepo.getSessionAvailableAt("123", now).isPresent, Is(false))

    }

    @Test
    fun shouldCountActiveSessions(){

        sessionRepo.issueSession(expiredSessionRequest)
        sessionRepo.issueSession(activeSessionRequest)
        
        assertThat(sessionRepo.getActiveSessionsCount(), Is(1))

    }

    @Test
    fun shouldReturnEmptyWhenNotFound(){
        assertThat(sessionRepo.
                getSessionAvailableAt("fakeSID", LocalDateTime.now())
                .isPresent, Is(false))
    }

}