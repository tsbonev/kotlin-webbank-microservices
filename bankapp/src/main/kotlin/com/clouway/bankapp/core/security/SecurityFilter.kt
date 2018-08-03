package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.SessionNotFoundException
import com.clouway.bankapp.core.SessionRepository
import org.eclipse.jetty.http.HttpStatus
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark.halt
import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class SecurityFilter(private val sessionRepo: SessionRepository,
                     private val sessionProvider: SessionProvider,
                     private val instant: LocalDateTime = LocalDateTime.now()) : Filter {

    private fun redirectToLogin(res: Response) {
        halt(HttpStatus.UNAUTHORIZED_401)
        res.redirect("/login")
    }

    override fun handle(req: Request, res: Response) {

        val cookie = req.cookie("SID") ?: return redirectToLogin(res)

        try {
            val possibleSession = sessionRepo.getSessionAvailableAt(cookie, instant)
            if(!possibleSession.isPresent) return redirectToLogin(res)
            sessionProvider.setContext(possibleSession.get())
        } catch (e: SessionNotFoundException) {

            when(req.pathInfo()){
                "/login" -> return
                "/register" -> return
                "/" -> return
            }

            redirectToLogin(res)
        }
    }

}