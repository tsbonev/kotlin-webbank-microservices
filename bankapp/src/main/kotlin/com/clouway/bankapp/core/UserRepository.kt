package com.clouway.bankapp.core

import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface UserRepository {

    fun getById(id: Long): Optional<User>

    fun deleteById(id: Long)

    fun update(user: User)

    fun getByUsername(username: String): Optional<User>

    @Throws(UserAlreadyExistsException::class)
    fun registerIfNotExists(registerRequest: UserRegistrationRequest) : User

    fun checkPassword(user: User): Boolean

}