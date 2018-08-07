package com.clouway.pubsub.factory

import com.clouway.pubsub.adapter.google.pubsub.AsyncPubsubEventBus
import com.clouway.pubsub.adapter.google.pubsub.PubsubEventConverter
import com.clouway.pubsub.adapter.google.pubsub.PubsubPublisherProvider
import com.clouway.pubsub.adapter.google.pubsub.PubsubRequestStreamReader
import com.clouway.pubsub.core.EventBus

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
object EventBusFactory {
    fun createAsyncPubsubEventBus(): EventBus {
        return AsyncPubsubEventBus(PubsubPublisherProvider(),
                PubsubEventConverter(),
                PubsubRequestStreamReader())
    }
}