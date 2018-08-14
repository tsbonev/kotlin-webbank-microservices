package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
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

    private val userChangeListener = context.mock(UserChangeListener::class.java)

    private val loginController = LoginController(userRepo,
            sessionRepository,
            jsonTransformer,
            getExpirationDate = {testDate},
            getCookieSID = {SID},
            listeners = userChangeListener)

    private val userController = UserController()
    private val registerController = RegisterController(userRepo, jsonTransformer, userChangeListener)

    private val logoutController = LogoutController(sessionRepository, userChangeListener)

    private val loginJSON = """
        {
        "username": "John",
        "password": "password"
        }
    """.trimIndent()

    private val registerJSON = """
        {
        "username": "John",
        "email":"john@email.com",
        "password": "password"
        }
    """.trimIndent()

    private val testUser = User(1L, "John", "email", "password")
    private val testUserRegistrationRequest = UserRegistrationRequest("John","email", "password")
    private val testUserLoginRequest = UserLoginRequest("John","password")
    private val possibleUser = Optional.of(testUser)
    private val testSession = Session(1L, SID, testDate, "John", "email")
    private var statusReturn: Int = 0

    private val registerReq = object: Request(){
        override fun body(): String {
            return registerJSON
        }
        override fun cookie(name: String): String{
            return SID
        }
    }

    private val loginReq = object: Request(){
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
                "email",
                testDate
        )

        context.expecting {
            oneOf(jsonTransformer).fromJson(loginJSON, UserLoginRequest::class.java)
            will(returnValue(testUserLoginRequest))
            oneOf(userRepo).getByUsername("John")
            will(returnValue(possibleUser))
            oneOf(sessionRepository).issueSession(testSessionRequest)
            oneOf(userChangeListener).onLogin(possibleUser.get().username)
        }

        loginController.handle(loginReq, res)
        assertThat(statusReturn == HttpStatus.OK_200, Is(true))

    }

    @Test
    fun rejectInvalidLoginCredentials(){
        val user = User(1L, "John", "email", "wrong pass")
        val possibleUser = Optional.of(user)

        context.expecting {
            oneOf(jsonTransformer).fromJson(loginJSON, UserLoginRequest::class.java)
            will(returnValue(testUserLoginRequest))
            oneOf(userRepo).getByUsername("John")
            will(returnValue(possibleUser))
        }

        loginController.handle(loginReq, res)
        assertThat(statusReturn == HttpStatus.UNAUTHORIZED_401, Is(true))
    }

    @Test
    fun userNotFoundInLogin(){
        val possibleUser = Optional.empty<User>()

        context.expecting {
            oneOf(jsonTransformer).fromJson(loginJSON, UserLoginRequest::class.java)
            will(returnValue(testUserLoginRequest))
            oneOf(userRepo).getByUsername("John")
            will(returnValue(possibleUser))
        }

        loginController.handle(loginReq, res)
        assertThat(statusReturn == HttpStatus.UNAUTHORIZED_401, Is(true))
    }

    @Test
    fun registerUserForFirstTime(){

        context.expecting {
            oneOf(jsonTransformer).fromJson(registerJSON, UserRegistrationRequest::class.java)
            will(returnValue(testUserRegistrationRequest))
            oneOf(userRepo)
                    .registerIfNotExists(testUserRegistrationRequest)
            will(returnValue(testUser))
            oneOf(userChangeListener).onRegistration(testUser)
        }

        registerController.handle(registerReq, res)
        assertThat(statusReturn == HttpStatus.CREATED_201, Is(true))

    }


    @Test
    fun rejectRegisteringTakenUsername(){

        context.expecting {
            oneOf(jsonTransformer).fromJson(registerJSON, UserRegistrationRequest::class.java)
            will(returnValue(testUserRegistrationRequest))
            oneOf(userRepo).registerIfNotExists(testUserRegistrationRequest)
            will(throwException(UserAlreadyExistsException()))
        }

        registerController.handle(registerReq, res)
        assertThat(statusReturn == HttpStatus.BAD_REQUEST_400, Is(true))

    }

    @Test
    fun logOutUser(){

        context.expecting {
            oneOf(sessionRepository).terminateSession(testSession.sessionId)
            oneOf(userChangeListener).onLogout(testSession.username, testSession.userEmail)
        }

        logoutController.handle(loginReq, res, testSession)
    }

    @Test
    fun retrieveSessionUser(){

        val user = userController.handle(loginReq, res, testSession)

        assertThat(user as User, Is(User(1L, "John", "email","")))

    }
}