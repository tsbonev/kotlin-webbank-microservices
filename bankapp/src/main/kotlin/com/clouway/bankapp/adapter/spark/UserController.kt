package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.User
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class UserController : SecureController {

    override fun handle(request: Request, response: Response, currentSession: Session): Any? {
        return User(
                currentSession.userId,
                currentSession.username,
                ""
        )
    }
}