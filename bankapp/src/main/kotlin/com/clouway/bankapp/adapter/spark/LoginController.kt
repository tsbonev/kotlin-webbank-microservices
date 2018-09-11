package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
import com.clouway.bankapp.core.*
import com.clouway.bankapp.core.security.PasswordHasher
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class LoginController(private val userRepo: Users,
                      private val sessions: Sessions,
                      private val serializer: JsonSerializer,
                      private val sessionLifetime: Long = 10,
                      private val getExpirationDate: () -> LocalDateTime = {
                          LocalDateTime.now().plusDays(sessionLifetime)
                      },
                      private val cookieLifetime: Int = 600000,
                      private val getCookieSID: () -> String = {
                          UUID.randomUUID().toString()
                      },
                      private val hasher: PasswordHasher,
                      private val listeners: UserChangeListener) : Controller {


    override fun handle(request: Request, response: Response): Any? {

        val loginRequest = serializer.fromJson(request.body(), UserLoginRequest::class.java)

        val possibleUser = userRepo.getByUsername(loginRequest.username)

        val SID = getCookieSID()

        return if (possibleUser.isPresent) {

            val retrievedUser = possibleUser.get()

            if (!hasher.matching(loginRequest.password, retrievedUser.password)) {
                return response.status(HttpStatus.UNAUTHORIZED_401)
            }

            sessions.issueSession(SessionRequest(
                    retrievedUser.id,
                    SID,
                    retrievedUser.username,
                    retrievedUser.email,
                    getExpirationDate()
            ))

            response.cookie("/", "SID", SID, cookieLifetime, false, true)
            listeners.onLogin(retrievedUser.username)
            response.status(HttpStatus.OK_200)
        } else {
            response.status(HttpStatus.UNAUTHORIZED_401)
        }
    }
}