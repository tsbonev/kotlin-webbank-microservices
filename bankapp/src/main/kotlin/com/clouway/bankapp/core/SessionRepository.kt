package com.clouway.bankapp.core

import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface SessionRepository {

    fun issueSession(sessionRequest: SessionRequest)
    fun terminateSession(sessionId: String)
    @Throws(SessionNotFoundException::class)
    fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session>
}