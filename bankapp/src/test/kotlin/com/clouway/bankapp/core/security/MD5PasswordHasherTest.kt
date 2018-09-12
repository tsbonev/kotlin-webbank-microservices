package com.clouway.bankapp.core.security

import com.clouway.bankapp.core.UserRegistrationRequest
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.Assert.assertThat
import java.util.*

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class MD5PasswordHasherTest {

    private val hasher = MD5PasswordHasher()

    private val registerRequest = UserRegistrationRequest("::username::", "::email::", "::password::")

    @Test
    fun hashAndMatchPassword(){
        val hashedRequest = hasher.hashRequest(registerRequest)

        assertThat(hasher.matching(registerRequest.password, hashedRequest.password), Is(true))
    }

}