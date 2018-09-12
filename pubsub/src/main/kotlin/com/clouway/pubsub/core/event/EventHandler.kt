package com.clouway.pubsub.core.event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface EventHandler {
    fun handle(eventWithAttributes: EventWithAttributes): Any?
}