package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.SessionNotFoundException
import com.clouway.bankapp.core.security.SessionProvider
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response
import spark.Route

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class SecuredController(private val controller: SecureController,
                        private val sessionProvider: SessionProvider) : Route {

    override fun handle(request: Request, response: Response): Any? {
        return try {
            val session = sessionProvider.getContext()
            return controller.handle(request, response, session)
        } catch (e: SessionNotFoundException) {
            response.status(HttpStatus.UNAUTHORIZED_401)
        }

    }
}