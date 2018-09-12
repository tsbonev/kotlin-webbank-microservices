package com.clouway.bankapp.core

import java.time.LocalDateTime
import java.util.Optional

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class InMemorySessions: Sessions {

    private val persistentMap = mutableMapOf<String, Session>()

    override fun issueSession(sessionRequest: SessionRequest): Session {
        val session = Session(
                sessionRequest.userId,
                sessionRequest.sessionId,
                sessionRequest.expiration,
                sessionRequest.username,
                sessionRequest.userEmail,
                true
        )
        persistentMap[session.sessionId] = session
        return session
    }

    override fun terminateSession(sessionId: String) {
        persistentMap.remove(sessionId)
    }

    override fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session> {
        val session = persistentMap[sessionId] ?: return Optional.empty()

        return if(session.expiresOn.isBefore(date)) Optional.empty()
        else Optional.of(session)
    }
}