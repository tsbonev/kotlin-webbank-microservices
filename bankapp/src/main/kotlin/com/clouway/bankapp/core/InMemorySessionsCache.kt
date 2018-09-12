package com.clouway.bankapp.core

import java.util.Optional

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class InMemorySessionsCache: Cache<Session> {

    private val cacheMap = mutableMapOf<String, Session>()

    override fun put(obj: Session): Session {
        cacheMap[obj.sessionId] = obj
        return obj
    }

    override fun get(key: String): Optional<Session> {
        val cachedSession = cacheMap[key] ?: return Optional.empty()
        return Optional.of(cachedSession)
    }

    override fun remove(key: String) {
        cacheMap.remove(key)
    }
}