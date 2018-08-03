package server

import com.clouway.mailservice.adapter.spark.MailController
import spark.Spark.post
import spark.servlet.SparkApplication

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AppBootstrap : SparkApplication {
    override fun init() {

        post("/api/v1/mail", MailController())

    }
}