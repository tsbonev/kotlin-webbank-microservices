package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.EventConverter
import com.clouway.pubsub.core.PublisherProvider
import com.clouway.pubsub.core.PublisherWrapper
import com.clouway.pubsub.core.Topic
import com.clouway.pubsub.core.event.Event
import com.google.appengine.api.utils.SystemProperty
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ListTopicsRequest
import com.google.pubsub.v1.ProjectName
import com.google.pubsub.v1.PubsubMessage

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal class PubsubTopic(private val topicName: String,
                           private val publisherProvider: PublisherProvider<PubsubMessage, Publisher>,
                           private val converter: EventConverter<PubsubMessage>)
    : Topic {
    
    init {
        if(SystemProperty.environment.value() == SystemProperty.Environment.Value.Production){
            TopicAdminClient.create().use { topicAdminClient ->
                val projectId = ServiceOptions.getDefaultProjectId()
                val listTopicRequest = ListTopicsRequest
                        .newBuilder()
                        .setProject(ProjectName.format(projectId))
                        .build()
                val response = topicAdminClient.listTopics(listTopicRequest)
                val topics = response.iterateAll()

                var topicExists = false

                topics.forEach{
                    if(it.name == topicName) topicExists = true
                }
                if(topicExists) topicAdminClient.createTopic(topicName)
            }
        }
    }
    
    override fun publish(event: Event) {
        val publisher: PublisherWrapper<PubsubMessage, Publisher> by lazy {publisherProvider.get(topicName)}

        try{
            val pubsubEvent = converter.convertEvent(event)
            publisher.publish(pubsubEvent)
        }finally {
            publisher.shutdown()
        }
    }
}