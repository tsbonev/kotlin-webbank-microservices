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

    fun register(registerRequest: UserRegistrationRequest) : User

    fun checkPassword(user: User): Boolean
}