package com.clouway.bankapp.core

import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class Session(val userId: Long,
                   val sessionId: String,
                   val expiresOn: LocalDateTime,
                   val username: String,
                   val isAuthenticated: Boolean = false)
