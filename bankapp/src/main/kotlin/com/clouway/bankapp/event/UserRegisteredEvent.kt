package com.clouway.bankapp.event

import com.clouway.kcqrs.core.Event
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */

data class UserRegisteredEvent(val username: String,
                               val email: String,
                               val password: String) : Event