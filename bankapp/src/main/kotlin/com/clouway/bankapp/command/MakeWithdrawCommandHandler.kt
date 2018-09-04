package com.clouway.bankapp.command

import com.clouway.bankapp.domain.Account
import com.clouway.kcqrs.core.AggregateNotFoundException
import com.clouway.kcqrs.core.CommandHandler
import com.clouway.kcqrs.core.EventCollisionException
import com.clouway.kcqrs.core.Repository
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MakeWithdrawCommandHandler (private val eventRepository: Repository) : CommandHandler<MakeWithdrawCommand> {
    override fun handle(command: MakeWithdrawCommand) {

        try{
            val id = command.id
            val account = eventRepository.getById(UUID.fromString(id), Account::class.java)
            account.makeWithdraw(command.amount)
            eventRepository.save(account)
        }catch (ex: AggregateNotFoundException){
            ex.printStackTrace()
        }catch (ex: EventCollisionException){
            ex.printStackTrace()
        }
    }
}