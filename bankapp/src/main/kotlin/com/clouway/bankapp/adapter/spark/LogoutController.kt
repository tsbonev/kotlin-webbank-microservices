package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionRepository
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class LogoutController(private val sessionRepository: SessionRepository,
                       private val listeners: UserChangeListener) : SecureController {

    override fun handle(request: Request, response: Response, currentSession: Session): Any? {
        sessionRepository.terminateSession(currentSession.sessionId)
        listeners.onLogout(currentSession.username, currentSession.userEmail)
        return response.status(HttpStatus.OK_200)
    }
}