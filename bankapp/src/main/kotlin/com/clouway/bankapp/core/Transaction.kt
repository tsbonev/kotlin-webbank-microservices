package com.clouway.bankapp.core

import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class Transaction (val id: String,
                        val operation: Operation,
                        val accountId: String,
                        val date: LocalDateTime,
                        val amount: Double)

