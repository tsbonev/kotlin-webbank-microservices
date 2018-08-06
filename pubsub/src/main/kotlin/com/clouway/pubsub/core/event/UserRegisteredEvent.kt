package com.clouway.pubsub.core.event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class UserRegisteredEvent (val userId: Long,
                                val username: String)