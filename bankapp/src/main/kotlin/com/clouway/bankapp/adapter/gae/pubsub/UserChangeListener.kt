package com.clouway.bankapp.adapter.gae.pubsub

import com.clouway.bankapp.core.User

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface UserChangeListener {
    fun onRegistration(user: User)
    fun onLogout(username: String, email: String)
    fun onLogin(username: String)
}