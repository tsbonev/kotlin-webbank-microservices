package com.clouway.bankapp.adapter.gae.pubsub

import com.clouway.pubsub.core.EventBus
import com.clouway.pubsub.core.event.DepositMadeEvent
import com.clouway.pubsub.core.event.WithdrawMadeEvent

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AsyncTransactionListener(private val eventBus: EventBus,
                               private val topic: String) : TransactionListener {

    override fun onDeposit(userId: String, amount: Double) {
        val depositEvent = DepositMadeEvent(userId,
                amount)
        eventBus.publish(depositEvent, DepositMadeEvent::class.java, topic)
    }

    override fun onWithdraw(userId: String, amount: Double) {
        val withdrawEvent = WithdrawMadeEvent(userId,
                amount)
        eventBus.publish(withdrawEvent, WithdrawMadeEvent::class.java, topic)
    }
}