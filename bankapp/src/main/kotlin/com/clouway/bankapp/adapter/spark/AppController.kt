package com.clouway.bankapp.adapter.spark

import spark.Request
import spark.Response
import spark.Route

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppController(private val controller: Controller) : Route {
    override fun handle(request: Request, response: Response): Any? {
        return controller.handle(request, response)
    }
}