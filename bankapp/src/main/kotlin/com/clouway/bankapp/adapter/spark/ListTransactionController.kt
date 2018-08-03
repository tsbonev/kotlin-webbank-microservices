package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.TransactionRepository
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import java.time.Instant

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class ListTransactionController(private val transactionRepo: TransactionRepository) : SecureController {

    override fun handle(request: Request, response: Response, currentSession: Session): Any? {
        val transactions = transactionRepo
                .getUserTransactions(currentSession.userId)
        response.status(HttpStatus.OK_200)
        return transactions
    }

}