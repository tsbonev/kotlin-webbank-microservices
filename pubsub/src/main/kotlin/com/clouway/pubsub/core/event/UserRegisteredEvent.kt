package com.clouway.pubsub.core.event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class UserRegisteredEvent (val userId: String,
                                val username: String,
                                val email: String) : Event