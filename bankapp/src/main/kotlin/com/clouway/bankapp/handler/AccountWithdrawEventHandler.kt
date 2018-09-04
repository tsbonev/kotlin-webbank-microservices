package com.clouway.bankapp.handler

import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.TransactionRepository
import com.clouway.bankapp.core.TransactionRequest
import com.clouway.bankapp.event.AccountDepositEvent
import com.clouway.bankapp.event.AccountWithdrawEvent
import com.clouway.kcqrs.core.EventHandler

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AccountWithdrawEventHandler(private val transactionRepo: TransactionRepository) : EventHandler<AccountWithdrawEvent> {
    override fun handle(event: AccountWithdrawEvent) {

        val transactionRequest = TransactionRequest(
                event.accountId,
                Operation.WITHDRAW,
                event.amount
        )

        transactionRepo.save(transactionRequest)
    }
}