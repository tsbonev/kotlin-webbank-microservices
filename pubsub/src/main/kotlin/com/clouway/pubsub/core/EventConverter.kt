package com.clouway.pubsub.core

import com.clouway.pubsub.core.event.Event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal interface EventConverter<out T> {
    fun convertEvent(event: Event, eventType: Class<*>): T
}