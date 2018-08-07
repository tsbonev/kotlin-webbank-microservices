package com.clouway.pubsub.core

import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import spark.Route

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface EventBus {
    fun publish(event: Event, eventType: Class<*>, topic: String)
    fun register(handlers: Map<Class<*>, EventHandler>): Route
    fun subscribe(topic: String, subscription: String, endpoint: String)
}