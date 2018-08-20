package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.UserRegistrationRequest

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface PasswordHasher {
    fun hashRequest(request: UserRegistrationRequest): UserRegistrationRequest
    fun matching(requestValue: String, hashedValue: String): Boolean
}