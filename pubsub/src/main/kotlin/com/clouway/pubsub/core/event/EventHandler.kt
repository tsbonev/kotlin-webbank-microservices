package com.clouway.pubsub.core.event

import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface EventHandler {
    fun handle(req: Request, res: Response): Any?
}