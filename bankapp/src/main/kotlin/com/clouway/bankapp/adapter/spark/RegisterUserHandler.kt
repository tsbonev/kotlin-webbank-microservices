package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.command.RegisterUserCommand
import com.clouway.bankapp.core.JsonSerializer
import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRegistrationRequest
import com.clouway.kcqrs.core.AggregateNotFoundException
import com.clouway.kcqrs.core.EventCollisionException
import com.clouway.kcqrs.core.HydrationException
import com.clouway.kcqrs.core.MessageBus
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class RegisterUserHandler(private val messageBus: MessageBus,
                          private val serializer: JsonSerializer) : Route {
    override fun handle(request: Request, response: Response): Any {

        return try{
            val registerRequest = serializer.fromJson(request.body(),
                    UserRegistrationRequest::class.java)

            messageBus.send(RegisterUserCommand(
                    registerRequest.username,
                    registerRequest.email,
                    registerRequest.password))

            response.status(HttpStatus.CREATED_201)
            return UserDTO(registerRequest.username, registerRequest.email)
        }catch (e: UserAlreadyExistsException){
            response.status(HttpStatus.UNPROCESSABLE_ENTITY_422)
        }catch (e: HydrationException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }catch (e: EventCollisionException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }catch (e: AggregateNotFoundException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }

    }
}

data class UserDTO(val username: String, val email: String)
