package com.clouway.pubsub.core

import com.clouway.pubsub.core.event.Event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface Topic {
    fun publish(event: Event)
}