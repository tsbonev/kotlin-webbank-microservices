package server

import com.clouway.mailservice.adapter.spark.MailController
import spark.Spark.get
import spark.Spark.post
import spark.servlet.SparkApplication

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppBootstrap : SparkApplication {
    override fun init() {

        post("/_ah/push-handlers/mail", MailController())

        get("/hello") { req, res ->
            "Hello from mail service"
        }

        get("/hello2") { req, res ->
            "Hello from mail service new version"
        }

    }
}