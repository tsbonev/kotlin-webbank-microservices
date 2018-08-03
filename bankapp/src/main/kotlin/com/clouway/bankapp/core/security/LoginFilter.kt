package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.SessionNotFoundException
import org.eclipse.jetty.http.HttpStatus
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark.halt

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class LoginFilter(private val sessionProvider: SessionProvider) : Filter {

    override fun handle(request: Request, response: Response) {
        return try{
            println("------${sessionProvider.getContext()}")
            halt(HttpStatus.FORBIDDEN_403)
            response.redirect("/user")
        }catch (e: SessionNotFoundException){
            println("------NO SESSION FOUND")
            response.redirect("/")
        }
    }
}