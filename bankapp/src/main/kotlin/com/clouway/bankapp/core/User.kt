package com.clouway.bankapp.core

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class User (val id: String, val username: String, val email: String, val password: String,
                 val accounts: List<String> = emptyList())