package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.Transaction
import com.clouway.bankapp.core.User
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.appengine.repackaged.com.google.gson.Gson
import org.eclipse.jetty.http.HttpStatus
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rule.SparkGaeDatastoreRule
import server.AppBootstrap
import spark.utils.IOUtils
import java.time.LocalDateTime
import org.hamcrest.CoreMatchers.`is` as Is

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class BankEndToEndTest {

    @Rule
    @JvmField
    val sparkRule = SparkGaeDatastoreRule(AppBootstrap())

    private val requestFactory = NetHttpTransport().createRequestFactory()

    private val host = "http://localhost:4567"

    private val registerJson = """
        {
        "username":"admin",
        "email":"admin@admin.com",
        "password":"password"
        }
        """.trimIndent()

    private val user = User(
            "::userId::",
            "admin",
            "admin@admin.com",
            ""
    )

    private val transactionJson = """
        {
        "operation": "WITHDRAW",
        "amount": "200.0"
        }
        """.trimIndent()

    private val loginJson = """
        {
        "username":"admin",
        "password":"password"
        }
        """.trimIndent()

    private val instant = LocalDateTime.of(1, 1, 1, 1, 1, 1)

    @Before
    fun setUpUser() {
        val request = requestFactory.buildPostRequest(GenericUrl("$host/register"),
                ByteArrayContent.fromString("application/json", registerJson))
        request.execute()
    }

    @Test
    fun happyPath() {
        val registerJson = """
        {
        "username":"user",
        "email":"user@user.com",
        "password":"password"
        }
        """.trimIndent()

        requestFactory.buildPostRequest(GenericUrl("$host/register"),
                ByteArrayContent.fromString("application/json", registerJson)).execute()

        val loginJson = """
        {
        "username":"user",
        "password":"password"
        }
        """.trimIndent()

        val loginResponse = requestFactory.buildPostRequest(GenericUrl("$host/login"),
                ByteArrayContent.fromString("application/json", loginJson)).execute()

        val cookie = loginResponse.headers["set-cookie"].toString().removePrefix("[").removeSuffix("]")

        val transactionJson = """
        {
        "operation": "WITHDRAW",
        "amount": "200.0"
        }
        """.trimIndent()

        val transaction = Transaction(
                "::transactionId::",
                Operation.WITHDRAW,
                "::userId::",
                instant,
                200.0,
                "user"
        )

        val addTransaction = requestFactory.buildPostRequest(GenericUrl("$host/transactions"),
                ByteArrayContent.fromString("application/json", transactionJson))
        addTransaction.headers.cookie = cookie
        addTransaction.execute()

        val viewTransactionRequest = requestFactory.buildGetRequest(GenericUrl("$host/transactions"))
        viewTransactionRequest.headers.cookie = cookie
        val viewTransactionResponse = viewTransactionRequest.execute()
        val transactions = Gson().fromJson(IOUtils.toString(viewTransactionResponse.content),
                Array<Transaction>::class.java)

        assertThat(transactions.size, Is(1))
        assertThat(transactions[0].copy(id = "::transactionId::", userId = "::userId::",
                date = instant), Is(transaction))
    }

    @Test
    fun registerNewUser() {
        val registerJson = """
        {
        "username":"::new-user::",
        "email":"newuser@user.com",
        "password":"password"
        }
        """.trimIndent()

        val response = requestFactory.buildPostRequest(GenericUrl("$host/register"),
                ByteArrayContent.fromString("application/json", registerJson)).execute()
        assertThat(response.statusCode, Is(HttpStatus.CREATED_201))
    }

    @Test
    fun rejectRepeatRegistration() {
        try {
            requestFactory.buildPostRequest(GenericUrl("$host/register"),
                    ByteArrayContent.fromString("application/json", registerJson)).execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.BAD_REQUEST_400))
        }
    }

    @Test
    fun rejectLogIn() {
        val loginJson = """
        {
        "username":"::fake-user::",
        "password":"password"
        }
        """.trimIndent()

        try {
            requestFactory.buildPostRequest(GenericUrl("$host/login"),
                    ByteArrayContent.fromString("application/json", loginJson)).execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.UNAUTHORIZED_401))
        }
    }

    @Test
    fun logInSuccessfully() {
        val response = requestFactory.buildPostRequest(GenericUrl("$host/login"),
                ByteArrayContent.fromString("application/json", loginJson)).execute()
        assertThat(response.statusCode, Is(HttpStatus.OK_200))
    }

    @Test
    fun rejectGetTransactions() {
        try {
            requestFactory.buildGetRequest(GenericUrl("$host/transactions")).execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.UNAUTHORIZED_401))
        }
    }

    @Test
    fun getTransactions() {
        val addRequest = requestFactory.buildPostRequest(GenericUrl("$host/transactions"),
                ByteArrayContent.fromString("application/json", transactionJson))

        val cookie = getAuthorizedCookie()

        addRequest.headers.cookie = cookie

        addRequest.execute()

        val transaction = Transaction(
                "::transactionId::",
                Operation.WITHDRAW,
                "::userId::",
                instant,
                200.0,
                "admin"
        )

        val getTransactionRequest = requestFactory.buildGetRequest(GenericUrl("$host/transactions"))

        getTransactionRequest.headers.cookie = cookie

        val getTransactionResponse = getTransactionRequest.execute()

        val transactions = Gson().fromJson(IOUtils.toString(getTransactionResponse.content),
                Array<Transaction>::class.java)

        assertThat(getTransactionResponse.statusCode, Is(HttpStatus.OK_200))
        assertThat(transactions.size, Is(1))
        assertThat(transactions[0].copy(id = "::transactionId::", userId = "::userId::",
                date = instant), Is(transaction))
    }

    @Test
    fun rejectPostTransaction() {
        try {
            requestFactory.buildPostRequest(GenericUrl("$host/transactions"),
                    ByteArrayContent.fromString("application/json", transactionJson)).execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.UNAUTHORIZED_401))
        }
    }

    @Test
    fun addTransaction() {
        val addRequest = requestFactory.buildPostRequest(GenericUrl("$host/transactions"),
                ByteArrayContent.fromString("application/json", transactionJson))

        val cookie = getAuthorizedCookie()

        addRequest.headers.cookie = cookie

        val addResponse = addRequest.execute()

        assertThat(addResponse.statusCode, Is(HttpStatus.CREATED_201))
    }

    @Test
    fun rejectGetUser() {
        try {
            requestFactory.buildGetRequest(GenericUrl("$host/user")).execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.UNAUTHORIZED_401))
        }
    }

    @Test
    fun getUser() {
        val getUserRequest = requestFactory.buildGetRequest(GenericUrl("$host/user"))
        val cookie = getAuthorizedCookie()

        getUserRequest.headers.cookie = cookie

        val getUserResponse = getUserRequest.execute()
        val userContent = Gson().fromJson(IOUtils.toString(getUserResponse.content),
                User::class.java)
        assertThat(getUserResponse.statusCode, Is(HttpStatus.OK_200))
        assertThat(userContent.copy(id = "::userId::"), Is(user))
    }

    @Test
    fun rejectGetActive() {
        try {
            requestFactory.buildGetRequest(GenericUrl("$host/active")).execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.UNAUTHORIZED_401))
        }
    }

    @Test
    fun getActiveUsers() {
        val request = requestFactory.buildGetRequest(GenericUrl("$host/active"))
        val cookie = getAuthorizedCookie()

        request.headers.cookie = cookie

        val response = request.execute()
        assertThat(response.statusCode, Is(HttpStatus.OK_200))
        val activeUserCount = Gson().fromJson(IOUtils.toString(response.content),
        Int::class.java)
        assertThat(activeUserCount, Is(1))
    }

    @Test
    fun rejectLogout() {
        try {
            requestFactory.buildPostRequest(GenericUrl("$host/logout"), null).execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.UNAUTHORIZED_401))
        }
    }

    @Test
    fun logUserOut() {
        val request = requestFactory.buildPostRequest(GenericUrl("$host/logout"), null)
        request.headers.cookie = getAuthorizedCookie()
        val response = request.execute()
        assertThat(response.statusCode, Is(HttpStatus.OK_200))
    }

    @Test
    fun rejectLoginIfAlreadyLoggedIn() {
        try {
            val request = requestFactory.buildPostRequest(GenericUrl("$host/login"),
                    ByteArrayContent.fromString("application/json", loginJson))
            request.headers.cookie = getAuthorizedCookie()
            request.execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.FORBIDDEN_403))
        }
    }

    @Test
    fun rejectRegisterIfLoggedIn() {
        try {
            val request = requestFactory.buildPostRequest(GenericUrl("$host/register"),
                    ByteArrayContent.fromString("application/json", registerJson))
            request.headers.cookie = getAuthorizedCookie()
            request.execute()
            fail("Request was not rejected with no authorization")
        } catch (ex: HttpResponseException) {
            assertThat(ex.statusCode, Is(HttpStatus.FORBIDDEN_403))
        }
    }

    private fun getAuthorizedCookie(): String {
        val response = requestFactory.buildPostRequest(GenericUrl("$host/login"),
                ByteArrayContent.fromString("application/json", loginJson)).execute()
        return response.headers["set-cookie"].toString().removePrefix("[").removeSuffix("]")
    }
}