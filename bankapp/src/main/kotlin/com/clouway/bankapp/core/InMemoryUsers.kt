package com.clouway.bankapp.core

import java.util.Optional
import java.util.UUID

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class InMemoryUsers : Users {

    private val persistentMap = mutableMapOf<String, User>()

    override fun getById(id: String): Optional<User> {
        val possibleUser = persistentMap[id] ?: return Optional.empty()
        return Optional.of(possibleUser)
    }

    override fun deleteById(id: String) {
        val possibleUser = persistentMap[id] ?: return
        persistentMap.remove(possibleUser.username)
        persistentMap.remove(possibleUser.id)
    }

    override fun update(user: User) {
        persistentMap[user.id] = user
        persistentMap[user.username] = user
    }

    override fun getByUsername(username: String): Optional<User> {
        val possibleUser = persistentMap[username] ?: return Optional.empty()
        return Optional.of(possibleUser)
    }

    @Throws(UserAlreadyExistsException::class)
    override fun registerIfNotExists(registerRequest: UserRegistrationRequest): User {
        if(persistentMap[registerRequest.username] != null) throw UserAlreadyExistsException()
        val user = User(
                UUID.randomUUID().toString(),
                registerRequest.username,
                registerRequest.email,
                registerRequest.password
        )
        persistentMap[user.id] = user
        persistentMap[user.username] = user
        return user
    }
}