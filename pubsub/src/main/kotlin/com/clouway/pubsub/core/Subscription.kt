package com.clouway.pubsub.core

import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
interface Subscription {
    fun<T: Event> registerEventHandler(eventType: Class<T>, eventHandler: EventHandler)
    fun handle(request: HttpServletRequest, response: HttpServletResponse): Any?
}