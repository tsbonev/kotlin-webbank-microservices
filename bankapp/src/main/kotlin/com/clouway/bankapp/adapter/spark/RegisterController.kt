package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
import com.clouway.bankapp.core.JsonSerializer
import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRegistrationRequest
import com.clouway.bankapp.core.UserRepository
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class RegisterController(private val userRepo: UserRepository,
                         private val serializer: JsonSerializer,
                         private val listeners: UserChangeListener) : Controller {

    override fun handle(request: Request, response: Response): Any {
        return try{

            val registerRequest = serializer.fromJson(request.body(),
                    UserRegistrationRequest::class.java)

            if(userRepo.getByUsername(registerRequest.username).isPresent) throw UserAlreadyExistsException()

            val user = userRepo.register(registerRequest)

            listeners.onRegistration(user)
            response.status(HttpStatus.CREATED_201)
        }catch (e: UserAlreadyExistsException){
            response.status(HttpStatus.BAD_REQUEST_400)
        }
    }
}