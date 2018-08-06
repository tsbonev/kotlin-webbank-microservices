package server

import com.clouway.mailservice.adapter.gae.PubsubDataReader
import com.clouway.mailservice.adapter.spark.MailController
import com.clouway.mailservice.adapter.spark.PubsubController
import spark.Spark.get
import spark.Spark.post
import spark.servlet.SparkApplication

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppBootstrap : SparkApplication {
    override fun init() {

        val dataReader = PubsubDataReader()
        val mailController = MailController()

        post("/_ah/push-handlers/pubsub/mail", PubsubController(mailController, dataReader))

        get("/hello") { req, res ->
            "Hello from mail service"
        }
    }
}