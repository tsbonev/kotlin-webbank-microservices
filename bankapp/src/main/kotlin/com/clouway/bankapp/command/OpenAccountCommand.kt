package com.clouway.bankapp.command

import com.clouway.kcqrs.core.Command

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class OpenAccountCommand(val userId: String, val accountId: String) : Command