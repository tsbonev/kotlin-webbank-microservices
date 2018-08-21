package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.command.MakeDepositCommand
import com.clouway.bankapp.command.MakeWithdrawCommand
import com.clouway.bankapp.core.*
import com.clouway.kcqrs.core.AggregateNotFoundException
import com.clouway.kcqrs.core.EventCollisionException
import com.clouway.kcqrs.core.HydrationException
import com.clouway.kcqrs.core.MessageBus
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MakeTransactionHandler(private val messageBus: MessageBus,
                             private val userRepo: UserRepository,
                             private val serializer: JsonSerializer): SecureController {
    override fun handle(request: Request, response: Response, currentSession: Session): Any? {

        return try{
            val transactionRequest = serializer.fromJson(request.body(), TransactionRequest::class.java)

            val userAccounts = userRepo.getById(currentSession.userId).get().accounts

            if(!userAccounts.contains(transactionRequest.accountId)){
                return response.status(HttpStatus.FORBIDDEN_403)
            }

            when(transactionRequest.operation){
                Operation.WITHDRAW -> messageBus.send(
                        MakeWithdrawCommand(transactionRequest.accountId, transactionRequest.amount)
                )
                Operation.DEPOSIT -> messageBus.send(
                        MakeDepositCommand(transactionRequest.accountId, transactionRequest.amount)
                )
            }

            response.status(HttpStatus.CREATED_201)
            return TransactionDTO(currentSession.userId, transactionRequest.amount, transactionRequest.operation)
        }catch (e: InsufficientFundsException){
            response.status(HttpStatus.UNPROCESSABLE_ENTITY_422)
        }catch (e: IllegalArgumentException){
            response.status(HttpStatus.BAD_REQUEST_400)
        }catch (e: HydrationException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }catch (e: EventCollisionException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }catch (e: AggregateNotFoundException){
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
        }
    }
}

data class TransactionDTO(val accountId: String, val amount: Double, val operation: Operation)