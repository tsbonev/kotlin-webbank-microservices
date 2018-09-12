package com.clouway.bankapp.core

import org.junit.Test
import java.time.LocalDateTime
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class SessionsDecoratorTest {

    private val sessionCache = InMemorySessionsCache()
    private val sessionRepository = InMemorySessions()
    private val sessions = SessionsDecorator(sessionCache,
            sessionRepository)

    private val instant = LocalDateTime.of(1, 1, 1, 1, 1, 1)

    private val sessionRequest = SessionRequest(
            "::userId::",
            "::sid::",
            "::username::",
            "::user-email::",
            instant
    )

    private val session = Session(
            "::userId::",
            "::sid::",
            instant,
            "::username::",
            "::user-email::",
            true
    )

    @Test
    fun returnSavedSession(){
        assertThat(sessions.issueSession(sessionRequest), Is(session))
    }

    @Test
    fun returnEmptyWhenExpiredSession(){
        sessions.issueSession(sessionRequest)

        assertThat(sessions.getSessionAvailableAt(sessionRequest.sessionId, instant.plusDays(1)).isPresent,
                Is(false))
    }

    @Test
    fun returnEmptyWhenNotFound(){
        assertThat(sessions.getSessionAvailableAt(sessionRequest.sessionId, instant.plusDays(1)).isPresent,
                Is(false))
    }

    @Test
    fun saveSessionInCache(){
        sessions.issueSession(sessionRequest)

        assertThat(sessionCache.get(sessionRequest.sessionId).isPresent, Is(true))
        assertThat(sessionCache.get(sessionRequest.sessionId).get(), Is(session))
    }

    @Test
    fun saveSessionInPersistence(){
        sessions.issueSession(sessionRequest)

        assertThat(sessionRepository.getSessionAvailableAt(sessionRequest.sessionId,
                instant.minusDays(1)).isPresent,
                Is(true))
        assertThat(sessionRepository.getSessionAvailableAt(sessionRequest.sessionId,
                instant.minusDays(1)).get(),
                Is(session))
    }

    @Test
    fun retrieveSession(){
        sessions.issueSession(sessionRequest)


        assertThat(sessions.getSessionAvailableAt(sessionRequest.sessionId,
                instant.minusDays(1)).isPresent, Is(true))
        assertThat(sessions.getSessionAvailableAt(sessionRequest.sessionId,
                instant.minusDays(1)).get(), Is(session))
    }

    @Test
    fun terminateSession(){
        sessions.issueSession(sessionRequest)

        sessions.terminateSession(sessionRequest.sessionId)

        assertThat(sessions.getSessionAvailableAt(sessionRequest.sessionId, instant.minusDays(1)).isPresent,
                Is(false))
    }

    @Test
    fun saveSessionToCacheIfRetrievedFromPersistence(){

        sessions.issueSession(sessionRequest)

        sessionCache.remove(sessionRequest.sessionId)

        sessions.getSessionAvailableAt(sessionRequest.sessionId,
                instant.minusDays(1))

        assertThat(sessionCache.get(sessionRequest.sessionId).isPresent, Is(true))
        assertThat(sessionCache.get(sessionRequest.sessionId).get(), Is(session))
    }
}