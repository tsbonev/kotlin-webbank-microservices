package com.clouway.bankapp.handler

import com.clouway.bankapp.core.User
import com.clouway.bankapp.core.UserNotFoundException
import com.clouway.bankapp.core.UserRepository
import com.clouway.bankapp.event.AccountOpenedEvent
import com.clouway.kcqrs.core.EventHandler

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AccountOpenedEventHandler(private val userViewRepo: UserRepository) : EventHandler<AccountOpenedEvent> {
    override fun handle(event: AccountOpenedEvent) {
        val possibleUser = userViewRepo.getById(event.userId)
        if(!possibleUser.isPresent) throw UserNotFoundException()
        val user = possibleUser.get()

        val updatedAccounts = user.accounts.plus(event.accountId)

        val updatedUser = User(
                user.id,
                user.username,
                user.email,
                user.password,
                updatedAccounts
        )
        userViewRepo.update(updatedUser)
    }
}