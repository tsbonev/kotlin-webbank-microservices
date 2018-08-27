package com.clouway.bankapp.adapter.gae.pubsub

import com.clouway.bankapp.core.Operation
import com.clouway.bankapp.core.User
import com.clouway.pubsub.core.EventBus
import com.clouway.pubsub.core.event.UserLoggedOutEvent
import com.clouway.pubsub.core.event.UserLoginEvent
import com.clouway.pubsub.core.event.UserRegisteredEvent
import com.clouway.pubsub.core.event.UserTransactionEvent
import com.google.appengine.api.utils.SystemProperty

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
class AsyncUserChangeListener(private val eventBus: EventBus,
                              private val topic: String) : UserChangeListener {
    override fun onLogin(username: String) {
        val userLoginEvent = UserLoginEvent(username)
        eventBus.publish(userLoginEvent, UserLoginEvent::class.java, topic)
    }

    override fun onTransaction(username: String, amount: Double, action: Operation) {
        val userTransactionEvent = UserTransactionEvent(username, amount, action.name)
        eventBus.publish(userTransactionEvent, UserTransactionEvent::class.java, topic)
    }

    override fun onLogout(username: String, email: String) {
        val userLoggedOutEvent = UserLoggedOutEvent(username, email)
        eventBus.publish(userLoggedOutEvent, UserLoggedOutEvent::class.java,topic)
    }

    override fun onRegistration(user: User) {
        val userRegisteredEvent = UserRegisteredEvent(user.id, user.username, user.email)
        eventBus.publish(userRegisteredEvent, UserRegisteredEvent::class.java, topic)
    }
}