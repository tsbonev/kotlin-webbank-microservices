package com.clouway.bankapp.adapter.gae.pubsub

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface TransactionListener {

    fun onDeposit(userId: String, amount: Double)
    fun onWithdraw(userId: String, amount: Double)

}