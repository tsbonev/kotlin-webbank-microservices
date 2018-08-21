package com.clouway.bankapp.event

import com.clouway.kcqrs.core.Event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AccountOpenedEvent (val userId: String,
                          val accountId: String) : Event