package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionNotFoundException
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class ThreadLocalSessionProvider : SessionProvider{

    private val sessionContext = ThreadLocal<Session>()

    override fun getContext(): Optional<Session> {
        return Optional.of(sessionContext.get())
    }

    override fun clearContext() {
        sessionContext.remove()
    }

    override fun setContext(context: Session) {
        sessionContext.set(context)
    }

}