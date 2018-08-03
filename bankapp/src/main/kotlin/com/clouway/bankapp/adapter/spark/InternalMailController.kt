package com.clouway.bankapp.adapter.spark

import com.google.appengine.api.taskqueue.QueueFactory
import com.google.appengine.api.taskqueue.TaskOptions
import org.eclipse.jetty.http.HttpStatus
import spark.Request
import spark.Response

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class InternalMailController : Controller {
    override fun handle(request: Request, response: Response): Any? {

        val queue = QueueFactory.getQueue("mailing-queue")
        queue.add(TaskOptions.Builder.withUrl("/mail")
                .param("email", request.queryParams("email")))

        return HttpStatus.ACCEPTED_202
    }
}