package com.clouway.bankapp.core.security

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

    override fun handle(req: Request, res: Response) {

        val cookie = req.cookie("SID")
                ?: return redirectTo(req, res, "/register")

        val possibleSession = sessionRepo.getSessionAvailableAt(cookie, instant)

        if (!possibleSession.isPresent) return redirectTo(req, res, "/login")

        sessionProvider.setContext(possibleSession.get())

        if (forbiddenAfterLoginPaths.contains(req.pathInfo())){
            halt(HttpStatus.FORBIDDEN_403)
            res.redirect("/user")
        }

    }


    private fun redirectTo(req: Request, res: Response, page: String) {
        if (openPaths.contains(req.pathInfo())) return
        halt(HttpStatus.UNAUTHORIZED_401)
        res.redirect(page)
    }

}
