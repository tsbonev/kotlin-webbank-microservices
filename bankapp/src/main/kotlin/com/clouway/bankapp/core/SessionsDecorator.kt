package com.clouway.bankapp.core

import java.time.LocalDateTime
import java.util.Optional

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class SessionsDecorator(private val cacheService: Cache<Session>,
                        private val decorated: Sessions) : Sessions {


    /**
     * Creates a session object and saves it into persistence and cache.
     *
     * @param sessionRequest The request to be converted to an object
     * @return The created Session object
     */
    override fun issueSession(sessionRequest: SessionRequest): Session {
        val session = decorated.issueSession(sessionRequest)
        cacheService.put(session)
        return session
    }

    /**
     * Removes a session from persistence and cache.
     *
     * @param sessionId The id of the session to be removed
     */
    override fun terminateSession(sessionId: String) {
        cacheService.remove(sessionId)
        decorated.terminateSession(sessionId)
    }

    /**
     * Retrieves a session from the cache or persistence, if a cached session
     * is not present, but a persistent session is, then saves the persistent session to the
     * cache.
     *
     * @param sessionId The id of the session
     * @param date The latest date the session can have as expiration
     * @return The optionally found session
     */
    override fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session> {
        val cachedSession = cacheService.get(sessionId)

        return if(cachedSession.isPresent){
            val retrievedSession = cachedSession.get()
            if(retrievedSession.expiresOn.isBefore(date)) Optional.empty()
            else cachedSession
        }else {
            val persistentSession = decorated.getSessionAvailableAt(sessionId, date)
            if(persistentSession.isPresent) cacheService.put(persistentSession.get())
            persistentSession
        }
    }
}