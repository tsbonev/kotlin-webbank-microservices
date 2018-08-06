package com.clouway.mailservice.adapter.spark

import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface Controller {
    fun handle(request: Request, response: Response): Any?
}