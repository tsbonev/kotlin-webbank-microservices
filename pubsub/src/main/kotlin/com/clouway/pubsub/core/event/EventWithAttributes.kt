package com.clouway.pubsub.core.event

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class EventWithAttributes (val event: Event, val attributes: Map<String, Any>)