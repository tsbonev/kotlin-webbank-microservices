package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.PublisherWrapper
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.PubsubMessage

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal class PubsubPublisherWrapper(private val publisher: Publisher): PublisherWrapper<PubsubMessage, Publisher> {

    override fun publish(message: PubsubMessage){
        publisher.publish(message)
    }

    override fun shutdown(){
        publisher.shutdown()
    }

    override fun raw(): Publisher{
        return publisher
    }
}