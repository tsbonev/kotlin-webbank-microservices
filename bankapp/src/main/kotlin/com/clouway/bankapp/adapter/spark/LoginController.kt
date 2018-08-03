package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.*
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import java.time.LocalDateTime
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class LoginController(private val userRepo: UserRepository,
                      private val sessionRepository: SessionRepository,
                      private val transformer: JsonSerializer,
                      private val sessionLifetime: Long = 10,
                      private val getExpirationDate: () -> LocalDateTime = {
                          LocalDateTime.now().plusDays(sessionLifetime)
                      },
                      private val cookieLifetime: Int = 600000,
                      private val getCookieSID: () -> String = {
                          UUID.randomUUID().toString()
                      }) : Controller {


    override fun handle(request: Request, response: Response): Any? {

        val loginRequest = transformer.fromJson(request.body(), UserRegistrationRequest::class.java)

        val actualUser = userRepo.getByUsername(loginRequest.username)

        val SID = getCookieSID()

        return if (actualUser.isPresent) {

            val user = actualUser.get()

            if (user.password != loginRequest.password) {
                return response.status(HttpStatus.UNAUTHORIZED_401)
            }

            sessionRepository.issueSession(SessionRequest(
                    user.id,
                    SID,
                    user.username,
                    getExpirationDate()
            ))

            response.cookie("/", "SID", SID, cookieLifetime, false, true)

            response.status(HttpStatus.OK_200)
        } else {
            response.status(HttpStatus.UNAUTHORIZED_401)
        }
    }
}