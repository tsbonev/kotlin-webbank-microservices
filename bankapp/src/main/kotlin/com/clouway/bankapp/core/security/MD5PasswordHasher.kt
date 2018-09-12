package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.UserRegistrationRequest
import org.eclipse.jetty.util.security.Credential

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MD5PasswordHasher : PasswordHasher {
    override fun matching(requestValue: String, hashedValue: String): Boolean {
        return Credential.MD5.digest(requestValue).removePrefix("MD5:") == hashedValue
    }

    override fun hashRequest(request: UserRegistrationRequest): UserRegistrationRequest {
        return UserRegistrationRequest(
                request.username,
                request.email,
                Credential.MD5.digest(request.password).removePrefix("MD5:")
        )
    }
}