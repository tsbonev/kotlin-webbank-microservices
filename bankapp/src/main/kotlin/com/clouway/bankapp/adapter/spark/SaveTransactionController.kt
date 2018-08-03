package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.JsonSerializer
import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.TransactionRepository
import com.clouway.bankapp.core.TransactionRequest
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class SaveTransactionController(private val transactionRepo: TransactionRepository,
                                private val transformer: JsonSerializer) : SecureController {

    override fun handle(request: Request, response: Response, currentSession: Session): Any? {
        response.type("application/json")

        return try{
            val transactionRequestFromJson = transformer.fromJson(request.body(), TransactionRequest::class.java)
            val completeTransactionRequest = TransactionRequest(
                    currentSession.userId,
                    transactionRequestFromJson.operation,
                    transactionRequestFromJson.amount)

            transactionRepo.save(completeTransactionRequest)
            response.status(HttpStatus.CREATED_201)
        }catch (e: IllegalStateException){
            response.status(HttpStatus.BAD_REQUEST_400)
        }
    }
}