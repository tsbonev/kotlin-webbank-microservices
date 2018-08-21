package com.clouway.bankapp.command

import com.clouway.kcqrs.core.Command

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MakeDepositCommand (val id: String,
                          val amount: Double) : Command