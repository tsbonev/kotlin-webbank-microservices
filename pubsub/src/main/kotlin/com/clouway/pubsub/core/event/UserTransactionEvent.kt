package com.clouway.pubsub.core.event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class UserTransactionEvent(val username: String, val amount: Double, val operation: String) : Event