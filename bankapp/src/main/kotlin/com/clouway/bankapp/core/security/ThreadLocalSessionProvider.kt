package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionNotFoundException

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class ThreadLocalSessionProvider : SessionProvider{

    private val sessionContext = ThreadLocal<Session>()

    override fun getContext(): Session {
        return sessionContext.get() ?: throw SessionNotFoundException()
    }

    override fun clearContext() {
        sessionContext.remove()
    }

    override fun setContext(context: Session) {
        sessionContext.set(context)
    }

}