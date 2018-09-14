package com.clouway.bankapp.adapter.gae.pubsub

import java.util.EventListener

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface TransactionListener : EventListener{

    fun onDeposit(userId: String, amount: Double)
    fun onWithdraw(userId: String, amount: Double)

}