package com.clouway.bankapp.adapter.gae.pubsub

import com.clouway.bankapp.core.User
import java.util.EventListener

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface UserChangeListener : EventListener {
    fun onRegistration(user: User)
    fun onLogout(username: String, email: String)
    fun onLogin(username: String)
}