package com.clouway.bankapp.core

import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class Transaction (val id: Long = -1,
                        val operation: Operation,
                        val userId: Long,
                        val date: LocalDateTime = LocalDateTime.now(),
                        val amount: Double,
                        val username: String = ""){

    fun getAmountFormatted(): String{
        return String.format("%.2f", amount)
    }
}

