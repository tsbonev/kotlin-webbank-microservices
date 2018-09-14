package com.clouway.pubsub.core.event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class WithdrawMadeEvent (val userId: String, val amount: Double) : Event