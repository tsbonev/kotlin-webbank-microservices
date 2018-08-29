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
                     private val openPaths: List<String> = emptyList(),
                     private val forbiddenAfterLoginPaths: List<String> = emptyList(),
                     private val instant: LocalDateTime = LocalDateTime.now()) : Filter {

    private fun redirectTo(res: Response, page: String, code: Int) {
        halt(code)
        res.redirect(page)
    }

    override fun handle(req: Request, res: Response) {


        try {
            val cookie = req.cookie("SID") ?: throw SessionNotFoundException()
            val possibleSession = sessionRepo.getSessionAvailableAt(cookie, instant)
            if(!possibleSession.isPresent) throw SessionNotFoundException()
            sessionProvider.setContext(possibleSession.get())

            if(forbiddenAfterLoginPaths.contains(req.pathInfo()))
                return redirectTo(res, "/user", HttpStatus.FORBIDDEN_403)

        } catch (e: SessionNotFoundException) {

            if(openPaths.contains(req.pathInfo())) return

            redirectTo(res, "/login", HttpStatus.UNAUTHORIZED_401)
        }
    }

}