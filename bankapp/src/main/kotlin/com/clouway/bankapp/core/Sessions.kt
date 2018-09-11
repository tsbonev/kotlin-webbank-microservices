package com.clouway.bankapp.core

import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface Sessions {

    fun issueSession(sessionRequest: SessionRequest): Session
    fun terminateSession(sessionId: String)
    fun getSessionAvailableAt(sessionId: String, date: LocalDateTime): Optional<Session>
}