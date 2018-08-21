package com.clouway.bankapp.handler

import com.clouway.bankapp.core.UserRegistrationRequest
import com.clouway.bankapp.core.UserRepository
import com.clouway.bankapp.event.UserRegisteredEvent
import com.clouway.kcqrs.core.EventHandler

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class UserRegisteredEventHandler(private val userViewRepo: UserRepository) : EventHandler<UserRegisteredEvent> {
    override fun handle(event: UserRegisteredEvent) {
        val registerRequest = UserRegistrationRequest(
                event.username,
                event.email,
                event.password
        )
        userViewRepo.register(registerRequest)
    }
}