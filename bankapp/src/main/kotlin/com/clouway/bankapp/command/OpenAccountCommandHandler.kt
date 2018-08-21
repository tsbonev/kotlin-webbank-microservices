package com.clouway.bankapp.command

import com.clouway.bankapp.domain.Account
import com.clouway.kcqrs.core.AggregateNotFoundException
import com.clouway.kcqrs.core.CommandHandler
import com.clouway.kcqrs.core.EventCollisionException
import com.clouway.kcqrs.core.Repository

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class OpenAccountCommandHandler(private val eventRepository: Repository) : CommandHandler<OpenAccountCommand> {
    override fun handle(command: OpenAccountCommand) {
        val account = Account(command.userId, command.accountId)
        try {
            eventRepository.save(account)
        }catch (ex: AggregateNotFoundException){
          ex.printStackTrace()
        }catch (ex: EventCollisionException){
            ex.printStackTrace()
        }
    }
}