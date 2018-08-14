package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.*
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.appengine.repackaged.com.google.gson.Gson
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.PushConfig
import spark.Route

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal class AsyncPubsubEventBus(private val publisherProvider: PublisherProvider<PubsubMessage, Publisher>,
                                   private val converter: EventConverter<PubsubMessage>,
                                   private val reader: RequestStreamReader<PubsubMessageWrapper>)
    : EventBus {

    override fun register(handlers: Map<Class<*>, EventHandler>): Route {
        return Route { req, res ->
            val messageWrapper = reader.read(req.raw().inputStream)
            val eventType = messageWrapper.message.getEventType()
            val handler = handlers[eventType]
            val event = Gson().fromJson(messageWrapper.message.decodeData(), eventType)
            req.attribute("event", event)
            handler!!.handle(req, res)
        }
    }

    override fun subscribe(topic: String, subscription: String, endpoint: String) {
        try{
            SubscriptionAdminClient.create().use { subscriptionAdminClient ->
                val projectId = ServiceOptions.getDefaultProjectId()
                val topicName = ProjectTopicName.of(projectId, topic)
                val subscriptionName = ProjectSubscriptionName.of(projectId, subscription)

                val pushConfig = PushConfig.newBuilder().setPushEndpoint(endpoint).build()

                val ackDeadlineInSeconds = 10

                subscriptionAdminClient.createSubscription(
                        subscriptionName, topicName, pushConfig, ackDeadlineInSeconds)
            }
        }catch (e: AlreadyExistsException){}
    }

    override fun publish(event: Event, eventType: Class<*>, topic: String) {

        val publisher: PublisherWrapper<PubsubMessage, Publisher> by lazy {publisherProvider.get(topic)}

        try{
            val convertedEvent = converter.convertEvent(event, eventType)
            publisher.publish(convertedEvent)
        }finally {
            publisher.shutdown()
        }
    }
}