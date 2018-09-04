package com.clouway.bankapp.domain

import com.clouway.bankapp.core.InsufficientFundsException
import com.clouway.bankapp.event.UserRegisteredEvent
import com.clouway.bankapp.event.AccountDepositEvent
import com.clouway.bankapp.event.AccountOpenedEvent
import com.clouway.bankapp.event.AccountWithdrawEvent
import com.clouway.kcqrs.core.AggregateRootBase
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class Account private constructor(var userId: String,
                                  var amount: Double) : AggregateRootBase() {

    // Required for the serialization
    constructor() : this("", 0.00)

    fun makeWithdraw(amount: Double) {
        if (this.amount >= amount) applyChange(AccountWithdrawEvent(getId()!!.toString(), amount))
        else throw InsufficientFundsException()
    }

    fun makeDeposit(amount: Double) {
        applyChange(AccountDepositEvent(getId()!!.toString(), amount))
    }

    constructor(userId: String, accountId: String)
            : this(userId, 0.00) {
        applyChange(AccountOpenedEvent(userId, accountId))
    }

    fun apply(event: AccountOpenedEvent) {
        uuid = UUID.fromString(event.accountId)
    }

    fun apply(event: AccountDepositEvent) {
        amount += event.amount
    }

    fun apply(event: AccountWithdrawEvent) {
        amount -= event.amount
    }
}