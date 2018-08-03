package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.Session
import com.clouway.bankapp.core.SessionNotFoundException

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface SessionProvider {

    @Throws(SessionNotFoundException::class)
    fun getContext(): Session
    fun setContext(context: Session)
    fun clearContext()

}