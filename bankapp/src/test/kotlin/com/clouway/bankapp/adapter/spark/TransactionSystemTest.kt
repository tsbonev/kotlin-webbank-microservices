package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
import com.clouway.bankapp.core.*
import com.clouway.bankapp.core.security.SessionProvider
import org.eclipse.jetty.http.HttpStatus
import org.jmock.AbstractExpectations.returnValue
import org.jmock.AbstractExpectations.throwException
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import spark.Request
import spark.Response
import java.time.LocalDateTime
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is


/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class TransactionSystemTest {

    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()

    private fun Mockery.expecting(block: Expectations.() -> Unit){
        checking(Expectations().apply(block))
    }

    private val transactionRepo = context.mock(Transactions::class.java)
    private val jsonSerializer = context.mock(JsonSerializer::class.java)
    private val userChangeListener = context.mock(UserChangeListener::class.java)

    private val testDate = LocalDateTime.of(2018, 8, 2, 10, 36, 23, 905000000)

    private val listTransactionController = ListTransactionController(transactionRepo)
    private val saveTransactionController = SaveTransactionController(transactionRepo, jsonSerializer, userChangeListener)

    private val SID = "123"
    private val testId = UUID.randomUUID().toString()
    private val testSession =
            Optional.of(Session(testId, SID, testDate, "John", "email",true))
    private var statusReturn: Int = 0
    private val sessionProvider = context.mock(SessionProvider::class.java)
    private val transactionRequest = TransactionRequest(testId, Operation.WITHDRAW, 200.0)
    private val transactionJson = """
        {
        "operation": "WITHDRAW",
        "amount": "200.0"
        }
    """.trimIndent()

    private val req = object: Request(){

        override fun headers(header: String?): String {
            return SID
        }

        override fun body(): String {
            return transactionJson
        }
        override fun cookie(name: String): String{
            return SID
        }
    }

    private val res = object: Response() {

        override fun type(contentType: String?) {

        }

        override fun status(statusCode: Int){
            statusReturn = statusCode
        }
    }

    @Test
    fun getUserTransactions(){

        context.expecting {
            oneOf(sessionProvider).getContext()
            will(returnValue(testSession))
            oneOf(transactionRepo).getUserTransactions(testId)
            will(returnValue(emptyList<Transaction>()))
        }

        SecuredController(listTransactionController, sessionProvider).handle(req, res)
        assertThat(statusReturn == HttpStatus.OK_200, Is(true))
    }

    @Test
    fun rejectCallWithNoSession(){

        context.expecting {
            oneOf(sessionProvider).getContext()
            will(throwException(SessionNotFoundException()))
        }

        SecuredController(listTransactionController, sessionProvider).handle(req, res)
        assertThat(statusReturn == HttpStatus.UNAUTHORIZED_401, Is(true))

    }

    @Test
    fun addTransactionToUserHistory(){

        context.expecting {
            oneOf(sessionProvider).getContext()
            will(returnValue(testSession))
            oneOf(jsonSerializer).fromJson(transactionJson, TransactionRequest::class.java)
            will(returnValue(transactionRequest))

            oneOf(transactionRepo).save(transactionRequest)
            oneOf(userChangeListener).onTransaction(testSession.get().username,
                    transactionRequest.amount,
                    transactionRequest.operation)

        }

        SecuredController(saveTransactionController, sessionProvider).handle(req, res)
        assertThat(statusReturn == HttpStatus.CREATED_201, Is(true))
    }

    @Test
    fun rejectAddingToMissingSession(){

        context.expecting {
            oneOf(sessionProvider).getContext()
            will(throwException(SessionNotFoundException()))
        }

        SecuredController(saveTransactionController, sessionProvider).handle(req, res)
        assertThat(statusReturn == HttpStatus.UNAUTHORIZED_401, Is(true))

    }
}