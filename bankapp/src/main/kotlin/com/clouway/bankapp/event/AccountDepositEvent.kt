package com.clouway.bankapp.event

import com.clouway.kcqrs.core.Event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class AccountDepositEvent(val accountId: String,
                               val amount: Double) : Event