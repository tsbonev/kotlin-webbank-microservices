package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.PublisherProvider
import com.clouway.pubsub.core.PublisherWrapper
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal class PubsubPublisherProvider : PublisherProvider<PubsubMessage, Publisher> {

    override fun get(topic: String): PublisherWrapper<PubsubMessage, Publisher> {
        val projectId = ServiceOptions.getDefaultProjectId()
        val topicName = ProjectTopicName.of(projectId, topic)

        val pubsubPublisher = Publisher.newBuilder(topicName).build()

        return PubsubPublisherWrapper(pubsubPublisher)
    }
}