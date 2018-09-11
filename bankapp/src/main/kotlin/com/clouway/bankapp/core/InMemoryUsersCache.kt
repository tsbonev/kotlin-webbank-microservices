package com.clouway.bankapp.core

import java.util.Optional

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class InMemoryUsersCache : Cache<User> {

    private val cacheMap = mutableMapOf<String, User>()

    override fun put(obj: User): User {
        cacheMap[obj.id] = obj
        cacheMap[obj.username] = obj
        return obj
    }

    override fun get(key: String): Optional<User> {
        val cachedUser = cacheMap[key] ?: return Optional.empty()
        return Optional.of(cachedUser)
    }

    override fun remove(key: String) {
        val cachedUser = cacheMap[key] ?: return
        cacheMap.remove(cachedUser.id)
        cacheMap.remove(cachedUser.username)
    }
}