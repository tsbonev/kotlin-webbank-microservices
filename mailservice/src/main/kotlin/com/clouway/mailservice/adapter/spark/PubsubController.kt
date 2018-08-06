package com.clouway.mailservice.adapter.spark

import com.clouway.mailservice.core.DataReader
import spark.Request
import spark.Response
import spark.Route

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class PubsubController(private val controller: Controller, private val dataReader: DataReader) : Route {
    override fun handle(request: Request, response: Response): Any? {
        request.attribute("data", dataReader.readData(request.raw()))
        return controller.handle(request, response)
    }
}