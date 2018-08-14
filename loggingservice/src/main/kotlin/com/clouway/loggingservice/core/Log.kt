package com.clouway.loggingservice.core

import com.clouway.pubsub.core.event.Event
import java.time.LocalDateTime

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
data class Log(val event: Event,
               val eventType: Class<*>,
               val time: LocalDateTime)