package com.clouway.bankapp.core

import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface SessionsClearer {
    fun deleteSessionsExpiringBefore(date: LocalDateTime)
}