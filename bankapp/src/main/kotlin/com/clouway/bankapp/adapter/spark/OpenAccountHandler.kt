package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.command.OpenAccountCommand
import com.clouway.bankapp.core.Session
import com.clouway.kcqrs.core.AggregateNotFoundException
import com.clouway.kcqrs.core.EventCollisionException
import com.clouway.kcqrs.core.HydrationException
import com.clouway.kcqrs.core.MessageBus
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import java.util.UUID

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class OpenAccountHandler(private val messageBus: MessageBus) : SecureController {
    override fun handle(request: Request, response: Response, currentSession: Session): Any? {

        return try{
            val accountId = UUID.randomUUID().toString()

            messageBus.send(OpenAccountCommand(currentSession.userId, accountId))

            response.status(HttpStatus.CREATED_201)
            return AccountDTO(accountId, currentSession.userId)
        }catch (e: HydrationException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }catch (e: EventCollisionException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }catch (e: AggregateNotFoundException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }
    }
}

data class AccountDTO(val accountId: String, val userId: String)