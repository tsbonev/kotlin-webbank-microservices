package com.clouway.pubsub.adapter.google.pubsub

import com.clouway.pubsub.core.RequestStreamReader
import com.clouway.pubsub.core.Subscription
import com.clouway.pubsub.core.event.Event
import com.clouway.pubsub.core.event.EventHandler
import com.clouway.pubsub.core.event.EventWithAttributes
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.appengine.api.utils.SystemProperty
import com.google.appengine.repackaged.com.google.gson.Gson
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PushConfig
import org.eclipse.jetty.http.HttpStatus
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Tsvetozar Bonev (tsbonev@gmail.com)
 */
internal class PubsubSubscription(private val subscriptionName: String,
                         private val topicName: String,
                         private val endpoint: String,
                         private val reader: RequestStreamReader<PubsubMessageWrapper>)
    : Subscription {

    private val handlers = mutableMapOf<String, EventHandler>()

    init {
        if(SystemProperty.environment.value() == SystemProperty.Environment.Value.Production){
            try{
                SubscriptionAdminClient.create().use { subscriptionAdminClient ->
                    val projectId = ServiceOptions.getDefaultProjectId()
                    val topicName = ProjectTopicName.of(projectId, topicName)
                    val subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionName)

                    val pushConfig = PushConfig.newBuilder().setPushEndpoint(endpoint).build()

                    val ackDeadlineInSeconds = 10

                    subscriptionAdminClient.createSubscription(
                            subscriptionName, topicName, pushConfig, ackDeadlineInSeconds)
                }
            }catch (e: AlreadyExistsException){}
        }
    }

    override fun <T : Event> registerEventHandler(eventType: Class<T>, eventHandler: EventHandler) {
        handlers[eventType.name] = eventHandler
    }

    override fun handle(request: HttpServletRequest, response: HttpServletResponse): Any? {
        val messageWrapper = reader.read(request.inputStream)
        val eventType = messageWrapper.message.getEventType()
        val handler = handlers[eventType.name]

        if(handler == null) {
            response.status = HttpStatus.NO_CONTENT_204
            return null
        }

        val event = Gson().fromJson(messageWrapper.message.decodeData(), eventType) as Event
        val eventWithAttributes = EventWithAttributes(
                event,
                mapOf(
                        "eventType" to eventType.name,
                        "time" to messageWrapper.message.getTime()
                )
        )

        return handler.handle(eventWithAttributes)
    }
}