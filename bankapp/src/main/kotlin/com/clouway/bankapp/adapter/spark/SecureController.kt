package com.clouway.bankapp.adapter.spark

import com.clouway.bankapp.core.Session
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface SecureController {

    fun handle(request: Request, response: Response, currentSession: Session): Any?

}