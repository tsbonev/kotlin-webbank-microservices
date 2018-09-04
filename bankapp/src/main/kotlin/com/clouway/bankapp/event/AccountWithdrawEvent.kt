package com.clouway.bankapp.event

import com.clouway.kcqrs.core.Event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AccountWithdrawEvent(val accountId: String,
                           val amount: Double) : Event