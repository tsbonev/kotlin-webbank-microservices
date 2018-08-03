package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.JsonSerializer
import com.clouway.bankapp.core.UserAlreadyExistsException
import com.clouway.bankapp.core.UserRegistrationRequest
import com.clouway.bankapp.core.UserRepository
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class RegisterController(private val userRepo: UserRepository,
                         private val transformer: JsonSerializer) : Controller {

    private val support : PropertyChangeSupport by lazy { PropertyChangeSupport(this) }

    fun addPropertyChangeListener(plc: PropertyChangeListener){
        support.addPropertyChangeListener(plc)
    }

    fun removePropertyChangeListener(plc: PropertyChangeListener){
        support.removePropertyChangeListener(plc)
    }

    override fun handle(request: Request, response: Response): Any? {
        return try{
            val user = userRepo
                    .registerIfNotExists(
                            transformer.fromJson(request.body(),
                                    UserRegistrationRequest::class.java))
            response.status(HttpStatus.CREATED_201)
            support.firePropertyChange("registration", null, user.username)
        }catch (e: UserAlreadyExistsException){
            response.status(HttpStatus.BAD_REQUEST_400)
        }
    }

}