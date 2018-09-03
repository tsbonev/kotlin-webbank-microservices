package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.adapter.gae.pubsub.TransactionListener
import com.clouway.bankapp.core.*
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class SaveTransactionController(private val transactionRepo: Transactions,
                                private val serializer: JsonSerializer,
                                private val listeners: TransactionListener) : SecureController {

    override fun handle(request: Request, response: Response, currentSession: Session): Any? {
        response.type("application/json")

        return try{
            val transactionRequestFromJson = serializer.fromJson(request.body(), TransactionRequest::class.java)
            val completeTransactionRequest = TransactionRequest(
                    currentSession.userId,
                    transactionRequestFromJson.operation,
                    transactionRequestFromJson.amount)

            transactionRepo.save(completeTransactionRequest)

            when(transactionRequestFromJson.operation){
                Operation.DEPOSIT -> listeners.onDeposit(currentSession.userId,
                        transactionRequestFromJson.amount)
                Operation.WITHDRAW -> listeners.onWithdraw(currentSession.userId,
                        transactionRequestFromJson.amount)
            }

            response.status(HttpStatus.CREATED_201)
        }catch (e: IllegalStateException){
            response.status(HttpStatus.BAD_REQUEST_400)
        }
    }
}