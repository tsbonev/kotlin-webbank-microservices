package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.*
import org.eclipse.jetty.http.HttpStatus
import org.jmock.AbstractExpectations.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.junit.Rule
import org.junit.Test
import org.jmock.integration.junit4.JUnitRuleMockery
import org.junit.Assert.assertThat
import spark.Request
import spark.Response
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.time.LocalDateTime
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is


/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class LoginSystemTest {

    @Rule
    @JvmField
    val context: JUnitRuleMockery = JUnitRuleMockery()

    private fun Mockery.expecting(block: Expectations.() -> Unit){
        checking(Expectations().apply(block))
    }

    private val userRepo = context.mock(UserRepository::class.java)
    private val sessionRepository = context.mock(SessionRepository::class.java)
    private val jsonTransformer = context.mock(JsonSerializer::class.java)

    private val SID = "123"
    private val testDate = LocalDateTime.now()

    private val loginController = LoginController(userRepo,
            sessionRepository,
            jsonTransformer,
            getExpirationDate = {testDate},
            getCookieSID = {SID})

    private val userController = UserController()

    private val registerController = RegisterController(userRepo, jsonTransformer)
    private val registerListener = context.mock(PropertyChangeListener::class.java)

    private val logoutController = LogoutController(sessionRepository)

    private val loginJSON = """
        {
        "username": "John",
        "password": "password"
        }
    """.trimIndent()

    private val testUser = User(1L, "John", "password")
    private val testUserRegistrationRequest = UserRegistrationRequest("John", "password")
    private val possibleUser = Optional.of(testUser)
    private val testSession = Session(1L, SID, testDate, "John")
    private var statusReturn: Int = 0

    private val req = object: Request(){
        override fun body(): String {
            return loginJSON
        }
        override fun cookie(name: String): String{
            return SID
        }
    }

    private val res = object: Response() {
        override fun status(statusCode: Int){
            statusReturn = statusCode
        }

        override fun cookie(path: String, name: String, value: String, maxAge: Int, secured: Boolean, httpOnly: Boolean) {

        }
    }

    @Test
    fun logInWithCorrectCredentials(){

        val testSessionRequest = SessionRequest(
                1,
                SID,
                "John",
                testDate
        )

        context.expecting {
            oneOf(jsonTransformer).fromJson(loginJSON, UserRegistrationRequest::class.java)
            will(returnValue(testUserRegistrationRequest))
            oneOf(userRepo).getByUsername("John")
            will(returnValue(possibleUser))
            oneOf(sessionRepository).issueSession(testSessionRequest)
        }

        loginController.handle(req, res)
        assertThat(statusReturn == HttpStatus.OK_200, Is(true))

    }

    @Test
    fun rejectInvalidLoginCredentials(){
        val user = User(1L, "John", "wrong pass")
        val possibleUser = Optional.of(user)

        context.expecting {
            oneOf(jsonTransformer).fromJson(loginJSON, UserRegistrationRequest::class.java)
            will(returnValue(testUserRegistrationRequest))
            oneOf(userRepo).getByUsername("John")
            will(returnValue(possibleUser))
        }

        loginController.handle(req, res)
        assertThat(statusReturn == HttpStatus.UNAUTHORIZED_401, Is(true))
    }

    @Test
    fun userNotFoundInLogin(){
        val possibleUser = Optional.empty<User>()

        context.expecting {
            oneOf(jsonTransformer).fromJson(loginJSON, UserRegistrationRequest::class.java)
            will(returnValue(testUserRegistrationRequest))
            oneOf(userRepo).getByUsername("John")
            will(returnValue(possibleUser))
        }

        loginController.handle(req, res)
        assertThat(statusReturn == HttpStatus.UNAUTHORIZED_401, Is(true))
    }

    @Test
    fun registerUserForFirstTime(){

        registerController.addPropertyChangeListener(registerListener)

        context.expecting {
            oneOf(jsonTransformer).fromJson(loginJSON, UserRegistrationRequest::class.java)
            will(returnValue(testUserRegistrationRequest))
            oneOf(userRepo)
                    .registerIfNotExists(testUserRegistrationRequest)
            will(returnValue(testUser))
            oneOf(registerListener).propertyChange(with(any(PropertyChangeEvent::class.java)))
        }

        registerController.handle(req, res)
        assertThat(statusReturn == HttpStatus.CREATED_201, Is(true))

    }


    @Test
    fun rejectRegisteringTakenUsername(){

        context.expecting {
            oneOf(jsonTransformer).fromJson(loginJSON, UserRegistrationRequest::class.java)
            will(returnValue(testUserRegistrationRequest))
            oneOf(userRepo).registerIfNotExists(testUserRegistrationRequest)
            will(throwException(UserAlreadyExistsException()))
        }

        registerController.handle(req, res)
        assertThat(statusReturn == HttpStatus.BAD_REQUEST_400, Is(true))

    }

    @Test
    fun logOutUser(){

        context.expecting {
            oneOf(sessionRepository).terminateSession(testSession.sessionId)
        }

        logoutController.handle(req, res, testSession)
    }

    @Test
    fun retrieveSessionUser(){

        val user = userController.handle(req, res, testSession)

        assertThat(user == User(1L, "John", ""), Is(true))

    }
}