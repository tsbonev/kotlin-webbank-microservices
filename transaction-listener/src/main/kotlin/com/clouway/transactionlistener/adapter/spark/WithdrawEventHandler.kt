package com.clouway.transactionlistener.adapter.spark

import com.clouway.pubsub.core.event.DepositMadeEvent
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.transactionlistener.core.Operation
import com.clouway.transactionlistener.core.Transaction
import com.clouway.transactionlistener.core.TransactionSaver
import com.google.appengine.repackaged.com.google.gson.Gson
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class WithdrawEventHandler(private val saver: TransactionSaver) : EventHandler {
    override fun handle(req: Request, res: Response): Any? {
        return try {
            val gson = Gson()
            val eventJson = gson.toJson(req.attribute<Event>("event"))
            val withdraw = gson.fromJson(eventJson, DepositMadeEvent::class.java)
            val withdrawTransaction = Transaction(
                    withdraw.userId,
                    withdraw.amount,
                    req.attribute<LocalDateTime>("time").toInstant(ZoneOffset.UTC).epochSecond,
                    Operation.WITHDRAW
            )
            saver.save(withdrawTransaction)
        } catch (e: Exception) {
            e.printStackTrace()
            HttpStatus.INTERNAL_SERVER_ERROR_500
        }
    }
}