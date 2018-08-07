package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.TopicCreator
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.cloud.ServiceOptions
import com.google.pubsub.v1.ProjectTopicName
import com.google.cloud.pubsub.v1.TopicAdminClient


/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal class PubsubTopicCreator : TopicCreator {
    override fun create(topic: String) {
        try{
            TopicAdminClient.create().use { topicAdminClient ->
                val projectId = ServiceOptions.getDefaultProjectId()
                val topicName = ProjectTopicName.of(projectId, topic)
                topicAdminClient.createTopic(topicName)
            }
        }catch (e: AlreadyExistsException){}
    }
}