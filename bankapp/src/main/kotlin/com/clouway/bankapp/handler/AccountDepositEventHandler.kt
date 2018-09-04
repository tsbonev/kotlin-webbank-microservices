package com.clouway.bankapp.handler

import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.TransactionRepository
import com.clouway.bankapp.core.TransactionRequest
import com.clouway.bankapp.event.AccountDepositEvent
import com.clouway.kcqrs.core.EventHandler

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AccountDepositEventHandler(private val transactionRepo: TransactionRepository) : EventHandler<AccountDepositEvent> {
    override fun handle(event: AccountDepositEvent) {

        val transactionRequest = TransactionRequest(
                event.accountId,
                Operation.DEPOSIT,
                event.amount
        )

        transactionRepo.save(transactionRequest)
    }
}