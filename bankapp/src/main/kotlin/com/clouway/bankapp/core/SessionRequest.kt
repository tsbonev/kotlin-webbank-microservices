package com.clouway.bankapp.core

import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class SessionRequest(val userId: String,
                          val sessionId: String,
                          val username: String,
                          val userEmail: String,
                          val expiration: LocalDateTime)