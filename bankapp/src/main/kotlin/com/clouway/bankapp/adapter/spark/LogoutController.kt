package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.adapter.gae.pubsub.UserChangeListener
import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.Sessions
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class LogoutController(private val sessions: Sessions,
                       private val listeners: UserChangeListener) : SecureController {

    override fun handle(request: Request, response: Response, currentSession: Session): Any? {
        sessions.terminateSession(currentSession.sessionId)
        listeners.onLogout(currentSession.username, currentSession.userEmail)
        return response.status(HttpStatus.OK_200)
    }
}