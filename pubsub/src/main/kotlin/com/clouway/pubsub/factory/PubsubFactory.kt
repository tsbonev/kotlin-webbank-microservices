package com.clouway.pubsub.factory

import com.clouway.pubsub.adapter.google.pubsub.*
import com.clouway.pubsub.core.Subscription
import com.clouway.pubsub.core.Topic

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
object PubsubFactory {
    fun createPubsubTopic(topicName: String): Topic{
        return PubsubTopic(topicName,
                PubsubPublisherProvider(),
                PubsubEventConverter())
    }
    fun createPubsubSubscription(topicName: String, subscriptionName: String, endpoint: String = "/_ah/push-handlers/pubsub/message"): Subscription{
        return PubsubSubscription(subscriptionName, topicName, endpoint, PubsubRequestStreamReader())
    }
}