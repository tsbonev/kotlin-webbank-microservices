package com.clouway.bankapp.adapter.gae.pubsub

import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.User
import com.clouway.pubsub.core.Topic
import com.clouway.pubsub.core.event.UserLoggedOutEvent
import com.clouway.pubsub.core.event.UserLoginEvent
import com.clouway.pubsub.core.event.UserRegisteredEvent
import com.clouway.pubsub.core.event.UserTransactionEvent

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AsyncUserChangeListener(private val topic: Topic) : UserChangeListener {
    override fun onLogin(username: String) {
        val userLoginEvent = UserLoginEvent(username)
        topic.publish(userLoginEvent)
    }

    override fun onTransaction(username: String, amount: Double, action: Operation) {
        val userTransactionEvent = UserTransactionEvent(username, amount, action.name)
        topic.publish(userTransactionEvent)
    }

    override fun onLogout(username: String, email: String) {
        val userLoggedOutEvent = UserLoggedOutEvent(username, email)
        topic.publish(userLoggedOutEvent)
    }

    override fun onRegistration(user: User) {
        val userRegisteredEvent = UserRegisteredEvent(user.id, user.username, user.email)
        topic.publish(userRegisteredEvent)
    }
}