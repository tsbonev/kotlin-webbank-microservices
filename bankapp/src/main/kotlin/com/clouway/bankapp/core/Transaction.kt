package com.clouway.bankapp.core

import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class Transaction (val id: String,
                        val operation: Operation,
                        val userId: String,
                        val date: LocalDateTime = LocalDateTime.now(),
                        val amount: Double,
                        val username: String = "")

