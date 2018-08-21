package com.clouway.bankapp.command

import com.clouway.kcqrs.core.Command
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class RegisterUserCommand(val username: String,
                               val email: String,
                               val password: String) : Command