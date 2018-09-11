package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
import com.clouway.bankapp.core.JsonSerializer
import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRegistrationRequest
import com.clouway.bankapp.core.Users
import com.clouway.bankapp.core.security.PasswordHasher
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class RegisterController(private val userRepo: Users,
                         private val transformer: JsonSerializer,
                         private val hasher: PasswordHasher,
                         private val listeners: UserChangeListener) : Controller {

    override fun handle(request: Request, response: Response): Any {
        return try{

            val registrationRequest = transformer.fromJson(request.body(),
                    UserRegistrationRequest::class.java)
            val hashedRequest = hasher.hashRequest(registrationRequest)

            val user = userRepo
                    .registerIfNotExists(hashedRequest)
            listeners.onRegistration(user)
            response.status(HttpStatus.CREATED_201)
        }catch (e: UserAlreadyExistsException){
            response.status(HttpStatus.BAD_REQUEST_400)
        }
    }
}