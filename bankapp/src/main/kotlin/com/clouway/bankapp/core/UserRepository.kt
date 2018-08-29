package com.clouway.bankapp.core

import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface UserRepository {

    fun getById(id: String): Optional<User>

    fun deleteById(id: String)

    fun update(user: User)

    fun getByUsername(username: String): Optional<User>

    @Throws(UserAlreadyExistsException::class)
    fun registerIfNotExists(registerRequest: UserRegistrationRequest) : User

    fun checkPassword(user: User): Boolean
}