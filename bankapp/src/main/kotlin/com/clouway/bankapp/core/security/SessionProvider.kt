package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionNotFoundException
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface SessionProvider {

    @Throws(SessionNotFoundException::class)
    fun getContext(): Optional<Session>
    fun setContext(context: Session)
    fun clearContext()

}