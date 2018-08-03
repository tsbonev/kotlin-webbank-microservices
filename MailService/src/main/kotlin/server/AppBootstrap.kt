package server

import com.clouway.mailservice.adapter.gae.MailSubscriber
import com.clouway.mailservice.adapter.spark.MailController
import spark.Route
import spark.Spark.get
import spark.Spark.post
import spark.kotlin.get
import spark.servlet.SparkApplication

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppBootstrap : SparkApplication {
    override fun init() {

        val mailSubscriber = MailSubscriber()

        post("/api/v1/mail", MailController())

        get("/hello") { req, res ->
            "Hello from mail service"
        }

    }
}