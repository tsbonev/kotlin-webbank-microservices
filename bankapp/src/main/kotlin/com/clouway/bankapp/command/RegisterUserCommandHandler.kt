package com.clouway.bankapp.command

import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRepository
import com.clouway.bankapp.event.UserRegisteredEvent
import com.clouway.kcqrs.core.*


/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class RegisterUserCommandHandler(private val messageBus: MessageBus,
                                 private val userRepository: UserRepository) : CommandHandler<RegisterUserCommand> {
    override fun handle(command: RegisterUserCommand) {
        val username = command.username
        val email = command.email
        val password = command.password

        val possibleUser = userRepository.getByUsername(username)
        if (possibleUser.isPresent) throw UserAlreadyExistsException()
        try {
            messageBus.handle(UserRegisteredEvent(
                    username,
                    email,
                    password
            ))
        }catch (ex: AggregateNotFoundException){
            ex.printStackTrace()
        }catch (ex: HydrationException){
            ex.printStackTrace()
        }catch (ex: EventCollisionException){
            ex.printStackTrace()
        }
    }
}
